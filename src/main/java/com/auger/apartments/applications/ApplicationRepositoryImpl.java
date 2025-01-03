package com.auger.apartments.applications;

import com.auger.apartments.exceptions.DatabaseException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ApplicationRowMapper applicationRowMapper;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ApplicationRepositoryImpl(JdbcTemplate jdbcTemplate, ApplicationRowMapper applicationRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationRowMapper = applicationRowMapper;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("applications")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Application create(Application application) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            LocalDate dateSubmitted = LocalDate.now();
            parameters.put("date_submitted", dateSubmitted);
            parameters.put("active", application.active());
            parameters.put("successful", application.successful());
            parameters.put("user_id", application.userId());
            parameters.put("apartment_id", application.apartmentId());

            int id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
            return new Application(id, dateSubmitted, application.active(), application.successful(),
                    application.userId(), application.apartmentId());
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when inserting an apartment in the database");
        }
    }

    @Override
    public Optional<Application> findOne(int id) {
        String sql = """
                SELECT *
                FROM applications
                WHERE id = ?
                LIMIT 1;
                """;
        return jdbcTemplate.query(sql, applicationRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Application> findAll() {
        String sql = """
                SELECT *
                FROM applications;
                """;
        return jdbcTemplate.query(sql, applicationRowMapper);
    }

    @Override
    public void update(Application application) {
        try {
            String sql = """
                    UPDATE applications
                    SET active = ?, successful = ?, user_id = ?, apartment_id = ?
                    WHERE id = ?;
                    """;
            jdbcTemplate.update(
                    sql,
                    application.active(), application.successful(), application.userId(), application.apartmentId(),
                    application.id()
            );
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when updating an apartment in the database");
        }
    }

    @Override
    public boolean exists(int id) {
        String sql = """
                SELECT COUNT(*)
                FROM applications
                WHERE id = ?;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }
}
