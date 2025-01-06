package com.auger.apartments.users;

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
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public User create(User user) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("first_name", user.firstName());
            parameters.put("last_name", user.lastName());
            parameters.put("email", user.email());
            parameters.put("phone_number", user.phoneNumber());
            parameters.put("birth_date", user.birthDate());
            LocalDate dateJoined = LocalDate.now();
            parameters.put("date_joined", dateJoined);

            int id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
            return new User(id, user.firstName(), user.lastName(), user.email(), user.phoneNumber(),
                    user.birthDate(), dateJoined);
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when inserting a user in the database");
        }
    }

    @Override
    public Optional<User> findOne(int id) {
        String sql = """
                SELECT *
                FROM users
                WHERE id = ?
                LIMIT 1;
                """;
        return jdbcTemplate.query(sql, userRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        String sql = """
                SELECT *
                FROM users;
                """;
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public void update(User user) {
        try {
            String sql = """
                UPDATE users
                SET first_name = ?, last_name = ?, email = ?, phone_number = ?, birth_date = ?
                WHERE id = ?;
                """;
            jdbcTemplate.update(
                    sql,
                    user.firstName(), user.lastName(), user.email(), user.phoneNumber(), user.birthDate(), user.id()
            );
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when updating a user in the database");
        }
    }

    @Override
    public void delete(int id) {
        try {
            String sql = """
                DELETE FROM users
                WHERE id = ?;
                """;
            jdbcTemplate.update(sql, id);
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when deleting a user in the database");
        }
    }

    @Override
    public boolean exists(int id) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE id = ?;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }
}
