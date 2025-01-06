package com.auger.apartments.apartments;

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
public class ApartmentRepositoryImpl implements ApartmentRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ApartmentRowMapper apartmentRowMapper;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ApartmentRepositoryImpl(JdbcTemplate jdbcTemplate, ApartmentRowMapper apartmentRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.apartmentRowMapper = apartmentRowMapper;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("apartments")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Apartment create(Apartment apartment) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("title", apartment.title());
            parameters.put("description", apartment.description());
            parameters.put("number_of_bedrooms", apartment.numberOfBedrooms());
            parameters.put("number_of_bathrooms", apartment.numberOfBathrooms());
            parameters.put("state", apartment.state());
            parameters.put("city", apartment.city());
            parameters.put("square_feet", apartment.squareFeet());
            parameters.put("monthly_rent", apartment.monthlyRent());
            LocalDate dateListed = LocalDate.now();
            parameters.put("date_listed", dateListed);
            parameters.put("available", apartment.available());
            parameters.put("owner_id", apartment.ownerId());
            parameters.put("renter_id", apartment.renterId());

            int id = simpleJdbcInsert.executeAndReturnKey(parameters).intValue();
            return new Apartment(id, apartment.title(), apartment.description(), apartment.numberOfBedrooms(),
                    apartment.numberOfBathrooms(), apartment.state(), apartment.city(), apartment.squareFeet(),
                    apartment.monthlyRent(), dateListed, apartment.available(), apartment.ownerId(),
                    apartment.renterId());
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when inserting an apartment in the database");
        }
    }

    @Override
    public Optional<Apartment> findOne(Integer id) {
        String sql = """
                SELECT *
                FROM apartments
                WHERE id = ?
                LIMIT 1;
                """;
        return jdbcTemplate.query(sql, apartmentRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Apartment> findAll() {
        String sql = """
                SELECT *
                FROM apartments;
                """;
        return jdbcTemplate.query(sql, apartmentRowMapper);
    }

    @Override
    public void update(Apartment apartment) {
        try {
            String sql = """
                UPDATE apartments
                SET title = ?, description = ?, number_of_bedrooms = ?, number_of_bathrooms = ?,
                state = ?, city = ?, square_feet = ?, monthly_rent = ?, available = ?,
                owner_id = ?, renter_id = ?
                WHERE id = ?;
                """;
            jdbcTemplate.update(
                    sql,
                    apartment.title(), apartment.description(), apartment.numberOfBedrooms(),
                    apartment.numberOfBathrooms(), apartment.state(), apartment.city(),
                    apartment.squareFeet(), apartment.monthlyRent(), apartment.available(),
                    apartment.ownerId(), apartment.renterId(), apartment.id()
            );
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when updating an apartment in the database");
        }
    }

    @Override
    public void delete(int id) {
        try {
            String sql = """
                DELETE FROM apartments
                WHERE id = ?;
                """;
            jdbcTemplate.update(sql, id);
        } catch (DataAccessException ex) {
            throw new DatabaseException("An error occurred when updating an apartment in the database");
        }
    }

    @Override
    public boolean exists(int id) {
        String sql = """
                SELECT COUNT(*)
                FROM apartments
                WHERE id = ?;
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }
}
