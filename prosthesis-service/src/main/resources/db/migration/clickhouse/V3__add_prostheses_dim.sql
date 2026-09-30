CREATE TABLE prosthesis.prostheses_dim
(
    prosthesis_id UUID,
    user_id UUID,
    serial_number String,
    model String,
    manufacturer String,
    installed_at Date,
    status String,
    updated_at DateTime64(3, 'UTC')
)
    ENGINE = ReplacingMergeTree(updated_at)
ORDER BY prosthesis_id;


CREATE TABLE prosthesis.telemetry_client_daily
(
    report_date Date,

    user_id UUID,
    prosthesis_id UUID,
    channel UInt16,

    samples_count UInt64,

    avg_amplitude Float64,
    min_amplitude Float64,
    max_amplitude Float64,

    avg_frequency Float64,
    min_frequency Float64,
    max_frequency Float64,

    first_event_time DateTime64(3, 'UTC'),
    last_event_time DateTime64(3, 'UTC')
)
    ENGINE = MergeTree
PARTITION BY toYYYYMM(report_date)
ORDER BY
(
    user_id,
    report_date,
    prosthesis_id,
    channel
);