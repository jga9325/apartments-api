DROP TABLE IF EXISTS "users";
DROP TABLE IF EXISTS "apartments";
DROP TABLE IF EXISTS "applications";

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name text NOT NULL,
    email text NOT NULL,
    phone_number text NOT NULL,
    birth_date date NOT NULL,
    date_joined date NOT NULL
);

CREATE TABLE apartments (
    id SERIAL PRIMARY KEY,
    name text NOT NULL,
    number_of_beds integer NOT NULL,
    number_of_bathrooms integer NOT NULL,
    state text NOT NULL,
    city text NOT NULL,
    square_feet integer NOT NULL,
    price_per_month_cents integer NOT NULL,
    date_listed date NOT NULL,
    available boolean NOT NULL,
    owner_id integer REFERENCES users NOT NULL,
    renter_id integer REFERENCES users UNIQUE
);

CREATE TABLE applications (
    id SERIAL PRIMARY KEY,
    date_submitted date NOT NULL,
    active boolean NOT NULL,
    successful boolean NOT NULL,
    apartment_id int REFERENCES apartments NOT NULL,
    user_id int REFERENCES users NOT NULL
);