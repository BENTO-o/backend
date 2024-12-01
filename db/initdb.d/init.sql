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

DROP TABLE IF EXISTS `note`;

CREATE TABLE `note` (
    note_id SERIAL PRIMARY KEY,
    title VARCHAR(50),
    content TEXT,
    folder VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
);

// dummy data
INSERT INTO `note` (title, content, folder) VALUES ('1주차 회의록', 'test1', '전공종합설계2')
INSERT INTO `note` (title, content, folder) VALUES ('2주차 회의록', 'test2', '전공종합설계2')
INSERT INTO `note` (title, content, folder) VALUES ('3주차 회의록', 'test3', '전공종합설계2')
INSERT INTO `note` (title, content, folder) VALUES ('4주차 회의록', 'test4', '전공종합설계2')
INSERT INTO `note` (title, content, folder) VALUES ('5주차 회의록', 'test5', '전공종합설계2')
INSERT INTO `note` (title, content, folder) VALUES ('6주차 회의록', 'test6', '전공종합설계2')
