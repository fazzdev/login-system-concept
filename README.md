# login-system-concept

## To start PostgreSQL container
1. Make sure `docker` is running
1. In your console, do `docker-compose up -d` from the root project to start PostgreSQL and Adminer in a container.

## Setup data using Adminer
1. In your browser, go to localhost:8080
1. When in Adminer page, select PostgreSQL and use `postgres:postgres` as username and password.
1. Import the data located in `resources/postgres.sql`.

To shutdown postgres do `docker-compose down` in your console.

