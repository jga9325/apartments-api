# apartments-api

## Summary

Backend implementation of a CRUD application that resembles a simplified Apartments.com.

This project exposes API endpoints which allow clients to create, view, edit, and delete users, 
apartments, and applications.

### Users of the application can:
- Create and manage their user profile
- List apartments that they own
- View apartments posted by other users
- Apply to apartments that interest them
- Browse and accept applications 

## How to use this project

This is a locally hosted project. In order to use it, you must create a local copy and run the application.
However, this process takes only a few simple steps thanks to Docker Compose.

### Requirements
- Docker Desktop (for Mac and Windows)

### Steps
1. Either clone or fork this repo to obtain a copy on your local machine
2. Navigate to the root repository of the project and open a command prompt/terminal
3. Run the Docker Desktop application
4. Enter the command `.\mvnw clean install` to install a JAR file in the target directory
5. Enter the command `docker compose -f compose.yaml up` to start the project
6. Once the project is running, you are able to send requests. For convenience, a Postman collection
   is provided in the file `postman_collection` in the root of the repository. Simply import this as a
   collection in Postman in order to have access to all the available requests. Additionally, complete
   documentation for all endpoints is listed below.
7. To view objects in the PostgreSQL database, navigate to `http://localhost:80` to access
   a running instance of pgAdmin. Directions for logging in are below.

## pgAdmin Login

1. Follow the steps above to run the application
2. Navigate to `http://localhost:80`
3. You should see the login box shown in the image below. Login with the following credentials:
   - Email Address / Username: `admin@domain.com`
   - Password: `password`

![A pgAdmin login box. There are two fields, one for Email Address / Username 
and another for Password](images/pgAdmin-login.jpg)

4. Click on `Add New Server`

![A button that says "Add New Server" with a red square surrounding it](images/add-new-server.jpg)

5. Under the `General` tab, add any name for the server to the `Name` field

![A popup box titled "Register Server" with several fields to enter data, including a field for a server 
name](images/register-server-1.jpg)

6. Under the `Connection` tab, fill out the following fields:
   - Host name/address: `postgresdb`
   - Port: `5432`
   - Maintenance database: `apartments-app`
   - Username: `username`
   - Password: `password`

![A popup box titled "Register Server" with several fields to enter](images/register-server-2.jpg)

7. Click `Save`
8. On the left hand side of the page the server wil be created. Open the server and navigate to 
   `Databases > apartments-app > Schemas > Tables`. You will see three tables titled users, apartments, 
   and applications.

# Apartments API Reference

The base url for all requests is `http://localhost`

### HTTP Status Code Summary
```
200         OK                      Everything worked as expected
201         Created                 Creation was successful
204         No Content              Success with no response

400         Bad Request             Invalid attribute/s provided
404         Not Found               Invalid id/s provided
409         Conflict                Attribute/s conflict with existing data

500         Internal Server Error   Something went wrong when accessing the database
```

## Users

### Create User

Create a new user

Endpoint: `POST` /users

**Body: User**
```
{
    "id": null,
    "firstName": "John",
    "lastName": "Rogers",
    "email": "john@gmail.com",
    "phoneNumber": "1234567894",
    "birthDate": "1999-04-28",
    "dateJoined": null
}
```

### Attributes
- **id**: integer or null
   - assigned by the application so null is acceptable
- **firstName**: string
- **lastName**: string
- **email**: string
   - must be unique
- **phoneNumber**: string
   - must be 10 digits with no spaces or dashes ex. 1234567890
   - must be unique
- **birthDate**: date
   - user must be between 18 and 100 years of age
- **dateJoined**: date
   - assigned by the application so null is acceptable

**Response Codes**
- `201` - created successfully
- `409` - there is a conflict between the provided attributes and existing data

**Response: User**
```
{
    "id": 1,
    "firstName": "John",
    "lastName": "Rogers",
    "email": "john@gmail.com",
    "phoneNumber": "1234567894",
    "birthDate": "1999-04-28",
    "dateJoined": "2025-01-10"
}
```

### Get User

Retrieve a user by id

Endpoint: `GET` /users/:id

Example: `/users/1`

**Response Codes**
- `200` - retrieved successfully
- `404` - user id is invalid

**Response: User**
```
{
    "id": 1,
    "firstName": "John",
    "lastName": "Rogers",
    "email": "john@gmail.com",
    "phoneNumber": "1234567894",
    "birthDate": "1999-04-28",
    "dateJoined": "2025-01-10"
}
```

### Get All Users

Retrieve a list of all users

Endpoint: `GET` /users

**Response Codes**
- `200` - retrieved successfully

**Response: List**
```
[
    {
        "id": 1,
        "firstName": "John",
        "lastName": "Rogers",
        "email": "john@gmail.com",
        "phoneNumber": "1234567894",
        "birthDate": "1999-04-28",
        "dateJoined": "2025-01-10"
    },
    {
        "id": 2,
        "firstName": "Bob",
        "lastName": "Smith",
        "email": "bob@gmail.com",
        "phoneNumber": "1234565894",
        "birthDate": "1993-04-28",
        "dateJoined": "2025-01-10"
    }
]
```

### Update User

Update a user

Endpoint: `PUT` /users

**Body: User**
```
{
    "id": 1,
    "firstName": "John",
    "lastName": "Rogers",
    "email": "john@gmail.com",
    "phoneNumber": "1234567894",
    "birthDate": "1999-04-28",
    "dateJoined": null
}
```

### Attributes
- **id**: integer
   - must reference an existing user
- **firstName**: string
- **lastName**: string
- **email**: string
   - must be unique
- **phoneNumber**: string
   - must be 10 digits with no spaces or dashes ex. 1234567890
   - must be unique
- **birthDate**: date
   - user must be between 18 and 100 years of age
- **dateJoined**: date
   - will not be updated so null is acceptable

**Response Codes**
- `204` - updated successfully
- `404` - user id is invalid
- `409` - there is a conflict between the provided attributes and existing data

**Response: Void**

### Delete User

Delete a user

Endpoint: `DELETE` /users/:id

Example: `/users/1`

**Response Codes**
- `204` - deleted successfully
- `404` - user id is invalid
- `409` - user does not meet the requirements for deletion

**Response: Void**

## Apartments

### Create Apartment

Create a new apartment

Endpoint: `POST` /apartments

**Body: Apartment**
```
{
    "id": null,
    "title": "Main Street Condo",
    "description": "A spacious condo with brand new appliances and great views!",
    "numberOfBedrooms": 2,
    "numberOfBathrooms": 1,
    "state": "NY",
    "city": "New York",
    "squareFeet": 800,
    "monthlyRent": 608900,
    "dateListed": null,
    "available": true,
    "ownerId": 1,
    "renterId": null
}
```

### Attributes
- **id**: integer or null
    - assigned by the application so null is acceptable
- **title**: string
- **description**: string
- **numberOfBedrooms**: integer
    - must be positive
- **numberOfBathrooms**: integer
    - must be positive
- **state**: string
- **city**: string
- **squareFeet**: integer
    - must be positive
- **monthlyRent**: integer
    - represented in cents (ex. $1200.00 should be passed in as 120000)
    - must be positive
- **dateListed**: date
    - assigned by the application so null is acceptable
- **available**: boolean
- **ownerId**: integer
    - must reference an existing user
- **renterId**: integer or null
    - must reference an existing user or null for an unoccupied apartment

**Response Codes**
- `201` - created successfully
- `409` - there is a conflict between the provided attributes and existing data

**Response: Apartment**
```
{
    "id": 1,
    "title": "Main Street Condo",
    "description": "A spacious condo with brand new appliances and great views!",
    "numberOfBedrooms": 2,
    "numberOfBathrooms": 1,
    "state": "NY",
    "city": "New York",
    "squareFeet": 800,
    "monthlyRent": 608900,
    "dateListed": "2025-01-10",
    "available": true,
    "ownerId": 1,
    "renterId": null
}
```

### Get Apartment

Retrieve an apartment by id

Endpoint: `GET` /apartments/:id

Example: `/apartments/1`

**Response Codes**
- `200` - retrieved successfully
- `404` - apartment id is invalid

**Response: Apartment**
```
{
    "id": 1,
    "title": "Main Street Condo",
    "description": "A spacious condo with brand new appliances and great views!",
    "numberOfBedrooms": 2,
    "numberOfBathrooms": 1,
    "state": "NY",
    "city": "New York",
    "squareFeet": 800,
    "monthlyRent": 608900,
    "dateListed": "2025-01-10",
    "available": true,
    "ownerId": 1,
    "renterId": null
}
```

### Get All Apartments

Retrieve a list of all apartments

Endpoint: `GET` /apartments

**Response Codes**
- `200` - retrieved successfully

**Response: List**
```
[
    {
        "id": 1,
        "title": "Main Street Condo",
        "description": "A spacious condo with brand new appliances and great views!",
        "numberOfBedrooms": 2,
        "numberOfBathrooms": 1,
        "state": "NY",
        "city": "New York",
        "squareFeet": 800,
        "monthlyRent": 608900,
        "dateListed": "2025-01-10",
        "available": true,
        "ownerId": 1,
        "renterId": null
    },
    {
        "id": 2,
        "title": "Beach Stay",
        "description": "Secluded home, perfect for a quiet and relaxing getaway.",
        "numberOfBedrooms": 2,
        "numberOfBathrooms": 2,
        "state": "HI",
        "city": "Honolulu",
        "squareFeet": 1500,
        "monthlyRent": 280000,
        "dateListed": "2025-01-10",
        "available": true,
        "ownerId": 1,
        "renterId": null
    }
]
```

### Update Apartment

Update an apartment

Endpoint: `PUT` /apartments

**Body: Apartment**
```
{
    "id": 1,
    "title": "Main Street Condo",
    "description": "Great location and insane views!",
    "numberOfBedrooms": 2,
    "numberOfBathrooms": 1,
    "state": "NY",
    "city": "New York",
    "squareFeet": 800,
    "monthlyRent": 645000,
    "dateListed": null,
    "available": true,
    "ownerId": 1,
    "renterId": null
}
```

### Attributes
- **id**: integer
    - must reference an existing apartment
- **title**: string
- **description**: string
- **numberOfBedrooms**: integer
    - must be positive
- **numberOfBathrooms**: integer
    - must be positive
- **state**: string
- **city**: string
- **squareFeet**: integer
    - must be positive
- **monthlyRent**: integer
    - represented in cents (ex. $1200.00 should be passed in as 120000)
    - must be positive
- **dateListed**: date
    - will not be updated so null is acceptable
- **available**: boolean
- **ownerId**: integer
    - must reference an existing user
- **renterId**: integer or null
    - must reference an existing user or null for an unoccupied apartment

**Response Codes**
- `204` - updated successfully
- `404` - apartment id is invalid
- `409` - there is a conflict between the provided attributes and existing data

**Response: Void**

### Delete Apartment

Delete an apartment

Endpoint: `DELETE` /apartments/:id

Example: `/apartments/1`

**Response Codes**
- `204` - deleted successfully
- `404` - apartment id is invalid
- `409` - apartment does not meet the requirements for deletion

**Response: Void**

## Applications

### Create Application

Create a new application

Endpoint: `POST` /applications

**Body: Application**
```
{
    "id": null,
    "dateSubmitted": null,
    "active": true,
    "successful": false,
    "userId": 1,
    "apartmentId": 1
}
```

### Attributes
- **id**: integer or null
    - assigned by the application so null is acceptable
- **dateSubmitted**: date
    - assigned by the application so null is acceptable
- **active**: boolean
- **successful**: boolean
- **userId**: integer
    - must reference an existing user
- **apartmentId**: integer
    - must reference an existing apartment

**Response Codes**
- `201` - created successfully
- `409` - there is a conflict between the provided attributes and existing data

**Response: Application**
```
{
    "id": 1,
    "dateSubmitted": "2025-01-10",
    "active": true,
    "successful": false,
    "userId": 1,
    "apartmentId": 1
}
```

### Get Application

Retrieve an application by id

Endpoint: `GET` /applications/:id

Example: `/applications/1`

**Response Codes**
- `200` - retrieved successfully
- `404` - application id is invalid

**Response: Application**
```
{
    "id": 1,
    "dateSubmitted": "2025-01-10",
    "active": true,
    "successful": false,
    "userId": 1,
    "apartmentId": 1
}
```

### Get All Applications

Retrieve a list of all applications

Endpoint: `GET` /applications

**Response Codes**
- `200` - retrieved successfully

**Response: List**
```
[
    {
        "id": 1,
        "dateSubmitted": "2025-01-10",
        "active": true,
        "successful": false,
        "userId": 1,
        "apartmentId": 1
    },
    {
        "id": 2,
        "dateSubmitted": "2025-01-10",
        "active": true,
        "successful": false,
        "userId": 2,
        "apartmentId": 1
    }
]
```

### Update Application

Update an application

Endpoint: `PUT` /applications

**Body: Application**
```
{
    "id": 1,
    "dateSubmitted": null,
    "active": false,
    "successful": true,
    "userId": 1,
    "apartmentId": 1
}
```

### Attributes
- **id**: integer
    - must reference an existing application
- **dateSubmitted**: date
    - will not be updated so null is acceptable
- **active**: boolean
- **successful**: boolean
- **userId**: integer
    - will not be updated so any integer is acceptable
- **apartmentId**: integer
    - will not be updated so any integer is acceptable

**Response Codes**
- `204` - updated successfully
- `404` - application id is invalid
- `409` - there is a conflict between the provided attributes and existing data

**Response: Void**

### Delete Application

Delete an application

Endpoint: `DELETE` /applications/:id

Example: `/applications/1`

**Response Codes**
- `204` - deleted successfully
- `404` - application id is invalid
- `409` - application does not meet the requirements for deletion

**Response: Void**

# Project Design and Technologies

## Tech Stack
- Java 17
- Spring Boot 3.4
- Maven
- PostgreSQL
- Docker and Docker Compose
- Testcontainers
- SLF4J and Logback
- pgAdmin

## Design

The application is separated into three layers. The presentation layer consists of controller classes which handle
incoming requests and direct them to the service layer. The service layer handles business logic such as data 
validation and then calls the persistence layer. Additionally, most exceptions are thrown from the service layer.
Lastly, the persistence layer executes SQL commands against a PostgreSQL database.

### Database

A PostgreSQL database stores users, apartments, and applications. Additionally, an instance of pgAdmin is provided 
in order to view the database.

This project uses Spring JDBC to execute SQL commands against the database.

A schema.sql file is used to create three tables in the database. These tables are dropped and recreated with each
run of the application, meaning that data does not persist between runs.

### Docker

A Dockerfile is used to Dockerize the application by importing a JAR file into a Java 17 image and executing it.

Docker Compose is used to run PostgreSQL, pgAdmin, and the application itself in three separate containers which 
run on the same Docker network.

By utilizing this strategy, it eliminates the need for users of the project to have Java 17, PostgreSQL, and pgAdmin
downloaded locally. The only requirement is to have Docker installed locally in order to run the application.

### Testing

This application includes 180 unit and integration tests to ensure expected functionality.

Unit tests use JUnit, Mockito, and Spring Boot annotations to test individual methods.

Integration tests utilize the Testcontainers project to access a PostgreSQL database, which allows tests to run against
a database of the same type and version that is being used in the project. Using the Singleton pattern, a single 
container is created and shared between all integration tests. This allows the integration test suite to run much more 
efficiently than if a new container was used for each test class. TestRestTemplate is used to make HTTP requests to 
controller methods and verify responses.

Lastly, abstract base classes are used to provide common resources to integration tests that need them, reducing
repeat code.

### Exception Handling

The @ControllerAdvice annotation is used to create a global exception handler class which catches all exceptions 
thrown within the application. This class defines @ExceptionHandler methods for all possible exceptions that might be 
thrown. Each method returns a Response Entity with an error message and an HTTP status code.