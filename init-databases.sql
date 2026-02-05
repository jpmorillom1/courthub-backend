
CREATE DATABASE courthub_auth;

CREATE DATABASE courthub_user;

CREATE DATABASE courthub_court;

CREATE DATABASE courthub_booking;

CREATE DATABASE courthub_payment;

GRANT ALL PRIVILEGES ON DATABASE courthub_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE courthub_user TO postgres;
GRANT ALL PRIVILEGES ON DATABASE courthub_court TO postgres;
GRANT ALL PRIVILEGES ON DATABASE courthub_booking TO postgres;
GRANT ALL PRIVILEGES ON DATABASE courthub_payment TO postgres;

