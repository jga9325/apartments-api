package com.auger.apartments.apartments;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ApartmentRowMapper implements RowMapper<Apartment> {

    @Override
    public Apartment mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Apartment(rs.getInt("id"),
                             rs.getString("title"),
                             rs.getString("description"),
                             rs.getInt("number_of_bedrooms"),
                             rs.getInt("number_of_bathrooms"),
                             rs.getString("state"),
                             rs.getString("city"),
                             rs.getInt("square_feet"),
                             rs.getInt("monthly_rent"),
                             rs.getDate("date_listed").toLocalDate(),
                             rs.getBoolean("available"),
                             rs.getInt("owner_id"),
                             rs.getObject("renter_id", Integer.class));
    }
}
