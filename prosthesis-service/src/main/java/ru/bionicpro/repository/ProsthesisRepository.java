package ru.bionicpro.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.bionicpro.model.CreateProsthesisRequest;
import ru.bionicpro.model.Prosthesis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class ProsthesisRepository {
    private final JdbcTemplate jdbc;

    public ProsthesisRepository(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Prosthesis create(UUID uid, CreateProsthesisRequest createProsthesisRequest) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO prostheses(id,user_id,serial_number,model,manufacturer,installed_at,status) VALUES (?,?,?,?,?,?,?)", id, uid, createProsthesisRequest.serialNumber(), createProsthesisRequest.model(), createProsthesisRequest.manufacturer(), createProsthesisRequest.installedAt(), createProsthesisRequest.status() == null ? "ACTIVE" : createProsthesisRequest.status());
        return findById(id);
    }

    public Prosthesis findById(UUID id) {
        return jdbc.queryForObject("SELECT id,user_id,serial_number,model,manufacturer,installed_at,status,created_at FROM prostheses WHERE id=?", (rs, n) -> map(rs), id);
    }

    public List<Prosthesis> findByUserId(UUID uid) {
        return jdbc.query("SELECT id,user_id,serial_number,model,manufacturer,installed_at,status,created_at FROM prostheses WHERE user_id=? ORDER BY created_at DESC", (rs, n) -> map(rs), uid);
    }

    private Prosthesis map(ResultSet resultSet) throws SQLException {
        return new Prosthesis(resultSet.getObject("id", UUID.class), resultSet.getObject("user_id", UUID.class), resultSet.getString("serial_number"), resultSet.getString("model"), resultSet.getString("manufacturer"), resultSet.getObject("installed_at", LocalDate.class), resultSet.getString("status"), resultSet.getObject("created_at", OffsetDateTime.class));
    }
}
