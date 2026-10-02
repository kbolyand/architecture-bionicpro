package ru.bionicpro.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bionicpro.model.EmgSample;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Repository
public class EmgSampleRepository {
    private final JdbcTemplate jdbc;

    public EmgSampleRepository(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertBatch(List<EmgSample> emgSampleList) {
        jdbc.batchUpdate("INSERT INTO emg_samples(event_time,prosthesis_id,sensor_id,channel,amplitude,frequency,sequence_id) VALUES (?,?,?,?,?,?,?)", emgSampleList, 1000, (p, x) -> {
            p.setTimestamp(1, Timestamp.from(x.eventTime()));
            p.setObject(2, x.prosthesisId());
            p.setString(3, x.sensorId());
            p.setInt(4, x.channel());
            p.setFloat(5, x.amplitude());
            p.setFloat(6, x.frequency());
            p.setLong(7, x.sequenceId());
        });
    }

    public List<EmgSample> findByProsthesis(UUID id, String sensor, int limit) {
        int l = Math.min(Math.max(limit, 1), 10000);
        String q = "SELECT event_time,user_id,prosthesis_id,sensor_id,channel,amplitude,frequency,sequence_id FROM emg_samples WHERE prosthesis_id=?" + (sensor == null ? "" : " AND sensor_id=?") + " ORDER BY event_time DESC LIMIT " + l;
        Object[] a = sensor == null ? new Object[]{id} : new Object[]{id, sensor};
        return jdbc.query(q, (r, n) -> new EmgSample(r.getTimestamp("event_time").toInstant(), r.getObject("prosthesis_id", UUID.class), r.getString("sensor_id"), r.getInt("channel"), r.getFloat("amplitude"), r.getFloat("frequency"), r.getLong("sequence_id")), a);
    }
}
