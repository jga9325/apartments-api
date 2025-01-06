DROP TABLE IF EXISTS "applications";
DROP TABLE IF EXISTS "apartments";
DROP TABLE IF EXISTS "users";

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name text NOT NULL,
    last_name text NOT NULL,
    email text NOT NULL UNIQUE,
    phone_number text NOT NULL UNIQUE,
    birth_date date NOT NULL,
    date_joined date NOT NULL
);

CREATE TABLE apartments (
    id SERIAL PRIMARY KEY,
    title text,
    description text,
    number_of_bedrooms integer NOT NULL,
    number_of_bathrooms integer NOT NULL,
    state text NOT NULL,
    city text NOT NULL,
    square_feet integer NOT NULL,
    monthly_rent integer NOT NULL,
    date_listed date NOT NULL,
    available boolean NOT NULL,
    owner_id integer REFERENCES users ON DELETE CASCADE NOT NULL,
    renter_id integer REFERENCES users UNIQUE
);

CREATE TABLE applications (
    id SERIAL PRIMARY KEY,
    date_submitted date NOT NULL,
    active boolean NOT NULL,
    successful boolean NOT NULL,
    user_id int REFERENCES users ON DELETE CASCADE NOT NULL,
    apartment_id int REFERENCES apartments ON DELETE CASCADE NOT NULL
);