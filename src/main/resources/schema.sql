DROP TABLE IF EXISTS "users";

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name text,
    email text,
    phone_number text,
    birth_date date,
    date_joined date
);