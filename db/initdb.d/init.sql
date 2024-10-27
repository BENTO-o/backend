DROP DATABASE IF EXISTS 'bento';

CREATE DATABASE 'bento';

USE 'bento';

DROP TABLE IF EXISTS 'user';

CREATE TABLE 'user' (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    oauth_provider VARCHAR(50),
    oauth_provider_id VARCHAR(50)
);
