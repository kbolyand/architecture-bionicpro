CREATE DATABASE IF NOT EXISTS prosthesis;

CREATE TABLE IF NOT EXISTS prosthesis.emg_samples
(
    event_time DateTime64(3, 'UTC'),
    prosthesis_id UUID,
    sensor_id String,
    channel UInt16,
    amplitude Float32,
    frequency Float32,
    sequence_id UInt64
    )
    ENGINE = MergeTree
    PARTITION BY toYYYYMM(event_time)
    ORDER BY (
                 prosthesis_id,
                 sensor_id,
                 channel,
                 event_time,
                 sequence_id
             );

CREATE TABLE IF NOT EXISTS prosthesis.prostheses_dim
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

CREATE TABLE IF NOT EXISTS prosthesis.telemetry_client_daily
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
    ORDER BY (
                 user_id,
                 report_date,
                 prosthesis_id,
                 channel
             );