from datetime import datetime, timedelta, timezone

import clickhouse_connect
import psycopg2
from airflow import DAG
from airflow.operators.python import PythonOperator
import logging


def get_pg_connection():
    import os
    return psycopg2.connect(
        host=os.environ["PG_HOST"],
        port=os.environ["PG_PORT"],
        dbname=os.environ["PG_DATABASE"],
        user=os.environ["PG_USER"],
        password=os.environ["PG_PASSWORD"],
        options=f"-c search_path={os.environ['PG_SCHEMA']},public"
    )


def get_ch_client():
    import os
    return clickhouse_connect.get_client(
        host=os.environ["CH_HOST"],
        port=int(os.environ["CH_PORT"]),
        username=os.environ["CH_USER"],
        password=os.environ["CH_PASSWORD"],
        database=os.environ["CH_DATABASE"],
    )


def sync_prostheses():
    pg = get_pg_connection()
    ch = get_ch_client()
    logger = logging.getLogger("airflow.task")

    try:
        from airflow.operators.python import get_current_context
        context = get_current_context()

        interval_start = context["data_interval_start"]
        interval_end = context["data_interval_end"]

        # 10-minute overlap protects against small scheduling/clock delays.
        lower_bound = interval_start - timedelta(minutes=10)

        sql = """
            SELECT
                id,
                user_id,
                serial_number,
                model,
                manufacturer,
                installed_at,
                status,
                updated_at
            FROM prostheses
            WHERE updated_at >= %s
              AND updated_at < %s
            ORDER BY updated_at, id
        """
        params = (lower_bound, interval_end)

        logger.info(
            "Executing PostgreSQL query: %s",
            " ".join(sql.split()),
        )
        logger.info(
            "Query interval: lower_bound=%s, interval_end=%s",
            lower_bound,
            interval_end,
        )

        with pg.cursor() as cur:
            cur.execute(sql, params)
            rows = cur.fetchall()

        selected_count = len(rows)

        logger.info(
            "PostgreSQL query completed: selected %d records "
            "(interval: %s - %s)",
            selected_count,
            lower_bound,
            interval_end,
        )

        if not rows:
            logger.info("No prostheses records to sync")
            return

        logger.info(
            "Inserting %d records into ClickHouse table prostheses_dim",
            selected_count,
        )

        ch.insert(
            "prostheses_dim",
            rows,
            column_names=[
                "prosthesis_id",
                "user_id",
                "serial_number",
                "model",
                "manufacturer",
                "installed_at",
                "status",
                "updated_at",
            ],
        )

        logger.info(
            "ClickHouse insert completed: %d records inserted into prostheses_dim",
            selected_count,
        )
    finally:
        pg.close()
        ch.close()


with DAG(
    dag_id="pg_to_clickhouse_prostheses",
    start_date=datetime(2026, 9, 30, tzinfo=timezone.utc),
    schedule="*/5 * * * *",
    catchup=False,
    max_active_runs=1,
    default_args={
        "owner": "data-platform",
        "retries": 2,
        "retry_delay": timedelta(minutes=2),
    },
    tags=["etl", "postgres", "clickhouse"],
) as dag:

    sync = PythonOperator(
        task_id="sync_prostheses",
        python_callable=sync_prostheses,
    )
