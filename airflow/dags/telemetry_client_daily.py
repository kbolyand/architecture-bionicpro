from datetime import datetime, timedelta, timezone
import logging  # Добавлен импорт для логирования

import clickhouse_connect
from airflow import DAG
from airflow.operators.python import PythonOperator

# Настройка логгера
logger = logging.getLogger(__name__)


def get_ch_client():
    import os
    return clickhouse_connect.get_client(
        host=os.environ["CH_HOST"],
        port=int(os.environ["CH_PORT"]),
        username=os.environ["CH_USER"],
        password=os.environ["CH_PASSWORD"],
        database=os.environ["CH_DATABASE"],
    )


def rebuild_daily_mart():
    """
    Rebuild affected dates instead of blindly INSERTing aggregates.

    This makes retries idempotent and handles late telemetry.
    The lookback window is configurable via TELEMETRY_LOOKBACK_MINUTES.
    """
    import os
    from airflow.operators.python import get_current_context

    context = get_current_context()
    interval_start = context["data_interval_start"]
    interval_end = context["data_interval_end"]

    lookback = int(os.environ.get("TELEMETRY_LOOKBACK_MINUTES", "15"))
    from_ts = interval_start - timedelta(minutes=lookback)
    to_ts = interval_end

    # Логирование временных интервалов
    logger.info("Processing telemetry slice. Lookback minutes: %s", lookback)
    logger.info("Data interval: from_ts = %s, to_ts = %s", from_ts, to_ts)

    ch = get_ch_client()

    try:
        # Recalculate the affected days. In the common hourly schedule this
        # normally touches one or two calendar days.
        affected_days = ch.query(
            """
            SELECT DISTINCT toDate(event_time) AS report_date
            FROM emg_samples
            WHERE event_time >= {from_ts:DateTime64(3)}
              AND event_time <  {to_ts:DateTime64(3)}
            """,
            parameters={
                "from_ts": from_ts,
                "to_ts": to_ts,
            },
        ).result_rows

        if not affected_days:
            logger.info("No affected days found for the given interval.")
            return

        logger.info("Affected days to rebuild: %s", [str(d[0]) for d in affected_days])

        for (report_date,) in affected_days:
            ch.command(
                """
                ALTER TABLE telemetry_client_daily
                DELETE WHERE report_date = {report_date:Date}
                SETTINGS mutations_sync = 2
                """,
                parameters={"report_date": report_date},
            )

        # Materialize the complete daily aggregates for affected dates.
        # This is intentionally a full-day rebuild because it is simple,
        # deterministic and correctly incorporates late events.
        rows = ch.query(
            """
            SELECT
                toDate(e.event_time) AS report_date,
                p.user_id AS user_id,
                e.prosthesis_id AS prosthesis_id,

                count() AS samples_count,

                avg(toFloat64(e.amplitude)) AS avg_amplitude,
                min(toFloat64(e.amplitude)) AS min_amplitude,
                max(toFloat64(e.amplitude)) AS max_amplitude,

                avg(toFloat64(e.frequency)) AS avg_frequency,
                min(toFloat64(e.frequency)) AS min_frequency,
                max(toFloat64(e.frequency)) AS max_frequency,

                min(e.event_time) AS first_event_time,
                max(e.event_time) AS last_event_time
            FROM emg_samples e
            INNER JOIN prostheses_dim p FINAL
                ON p.prosthesis_id = e.prosthesis_id
            WHERE toDate(e.event_time) IN (
                SELECT DISTINCT toDate(event_time)
                FROM emg_samples
                WHERE event_time >= {from_ts:DateTime64(3)}
                  AND event_time <  {to_ts:DateTime64(3)}
            )
            GROUP BY
                report_date,
                p.user_id,
                e.prosthesis_id
            """,
            parameters={
                "from_ts": from_ts,
                "to_ts": to_ts,
            },
        ).result_rows

        if rows:
            logger.info("Inserting %s aggregated rows into telemetry_client_daily.", len(rows))
            ch.insert(
                "telemetry_client_daily",
                rows,
                column_names=[
                    "report_date",
                    "user_id",
                    "prosthesis_id",
                    "samples_count",
                    "avg_amplitude",
                    "min_amplitude",
                    "max_amplitude",
                    "avg_frequency",
                    "min_frequency",
                    "max_frequency",
                    "first_event_time",
                    "last_event_time",
                ],
            )
    finally:
        ch.close()


with DAG(
    dag_id="telemetry_client_daily",
    start_date=datetime(2026, 9, 30, tzinfo=timezone.utc),
    schedule="0 * * * *",
    catchup=False,
    max_active_runs=1,
    default_args={
        "owner": "data-platform",
        "retries": 2,
        "retry_delay": timedelta(minutes=2),
    },
    tags=["etl", "telemetry", "mart", "clickhouse"],
) as dag:

    build_mart = PythonOperator(
        task_id="rebuild_daily_mart",
        python_callable=rebuild_daily_mart,
    )