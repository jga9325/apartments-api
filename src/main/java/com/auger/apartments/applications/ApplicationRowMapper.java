package com.auger.apartments.applications;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ApplicationRowMapper implements RowMapper<Application> {

    @Override
    public Application mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Application(rs.getInt("id"),
                               rs.getDate("date_submitted").toLocalDate(),
                               rs.getBoolean("active"),
                               rs.getBoolean("successful"),
                               rs.getInt("user_id"),
                               rs.getInt("apartment_id"));
    }
}
