INSERT INTO prosthesis.emg_samples
SELECT
-- От 1 дня назад до 3 дней вперёд
    now64(3)
        - INTERVAL 1 DAY
    + toIntervalMillisecond(
    toInt64(
     (number % 10000)
    * (4 * 24 * 60 * 60 * 1000)
    / 9999
    )
    ) AS event_time,

-- Каждый протез получает ровно 10000 записей
     [
    toUUID('87e81902-6b66-459e-9e26-9582c1ea685b'),
    toUUID('99fe72f6-605b-4cbd-b83c-01a882a0a2d8'),
    toUUID('8b99dc20-e5c6-42b6-b6b6-65f1edb850a4'),
    toUUID('61a7e8a8-51bd-4aaa-8610-0b4e13c4319c'),
    toUUID('2380019f-63d7-49c9-8285-c10b829f8182'),
    toUUID('8c1e4242-6903-4b97-8b03-b3d0eff2cdfb'),
    toUUID('fcb571cc-e929-4c71-b498-7c4957ba2325'),
    toUUID('3ad2807a-8e93-4db0-a387-19a50d617da5'),
    toUUID('1228bd0f-082e-41a9-bc41-c6f0c7367878'),
    toUUID('f6b7dabe-3e71-4046-818d-ba8878a1b00e')
    ][intDiv(number, 10000) + 1] AS prosthesis_id,

-- Случайный датчик
    concat(
    'sensor_',
    toString(toUInt32(floor(randCanonical() * 4)) + 1)
    ) AS sensor_id,

-- Канал 1..10
    toUInt16(floor(randCanonical() * 10) + 1) AS channel,

-- Амплитуда 0..5 мВ
    round(randCanonical() * 5, 4) AS amplitude,

-- Частота 10..500 Гц
    round(10 + randCanonical() * 490, 2) AS frequency,

-- Порядковый номер внутри протеза
    number % 10000 AS sequence_id

FROM numbers(100000);