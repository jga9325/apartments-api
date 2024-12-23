package com.auger.apartments.users;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

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
        // Add exception handling that throws custom exception for DuplicateKeyException
        // and a generic exception for DataAccessException

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", user.name());
        parameters.put("email", user.email());
        parameters.put("phone_number", user.phoneNumber());
        parameters.put("birth_date", user.birthDate());
        parameters.put("date_joined", user.dateJoined());

        int id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
        return new User(id, user.name(), user.email(), user.phoneNumber(), user.birthDate(), user.dateJoined());
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
    public int update(User user) {
        // Add exception handling that throws custom exception for DuplicateKeyException
        // and a generic exception for DataAccessException

        String sql = """
                UPDATE users
                SET name = ?, email = ?, phone_number = ?, birth_date = ?
                WHERE id = ?;
                """;
        return jdbcTemplate.update(
                sql,
                user.name(), user.email(), user.phoneNumber(), user.birthDate(), user.id()
        );
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
