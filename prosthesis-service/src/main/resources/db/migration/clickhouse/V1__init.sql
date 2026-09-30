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