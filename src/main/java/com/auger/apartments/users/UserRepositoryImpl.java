package com.auger.apartments.users;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public int create(User user) {
        String sql = """
                INSERT INTO users (name, email, phone_number, birth_date, date_joined)
                VALUES (?, ?, ?, ?, ?);
                """;
        return jdbcTemplate.update(
                sql,
                user.name(), user.email(), user.phoneNumber(), user.birthDate(), user.dateJoined()
        );
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
