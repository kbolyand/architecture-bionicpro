package ru.bionicpro.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.bionicpro.model.TelemetryClientDaily;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class TelemetryRepository {
    private final JdbcTemplate jdbc;

    public TelemetryRepository(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TelemetryClientDaily> getByUserId(UUID userId) {
        String sql = """
                    SELECT 
                        report_date,
                        user_id,
                        prosthesis_id,
                        channel,
                        samples_count,
                        avg_amplitude,
                        min_amplitude,
                        max_amplitude,
                        avg_frequency,
                        min_frequency,
                        max_frequency,
                        first_event_time,
                        last_event_time
                    FROM prosthesis.telemetry_client_daily
                    WHERE user_id = ?
                """;

        return jdbc.query(sql, new Object[]{userId.toString()}, new TelemetryRowMapper());
    }

    /**
     * Маппер данных ResultSet в Java Record
     */
    private static class TelemetryRowMapper implements RowMapper<TelemetryClientDaily> {
        @Override
        public TelemetryClientDaily mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new TelemetryClientDaily(
                    rs.getObject("report_date", LocalDate.class),
                    UUID.fromString(rs.getString("user_id")),
                    UUID.fromString(rs.getString("prosthesis_id")),
                    rs.getInt("channel"),
                    rs.getLong("samples_count"),
                    rs.getDouble("avg_amplitude"),
                    rs.getDouble("min_amplitude"),
                    rs.getDouble("max_amplitude"),
                    rs.getDouble("avg_frequency"),
                    rs.getDouble("min_frequency"),
                    rs.getDouble("max_frequency"),
                    rs.getObject("first_event_time", LocalDateTime.class),
                    rs.getObject("last_event_time", LocalDateTime.class)
            );
        }
    }
}
