CREATE DATABASE chat_app;
USE chat_app;

CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       username VARCHAR(255),
                       password VARCHAR(255),
                       nick_name VARCHAR(255),
                       profile_picture VARCHAR(255),
                       is_admin BOOLEAN DEFAULT FALSE
);

CREATE TABLE chats (
                       chat_id INT PRIMARY KEY AUTO_INCREMENT,
                       start_time DATETIME,
                       end_time DATETIME,
                       file_path VARCHAR(255)
);

CREATE TABLE subscriptions (
                               user_id INT,
                               chat_id INT,
                               subscription_time DATETIME,
                               FOREIGN KEY (user_id) REFERENCES users(id),
                               FOREIGN KEY (chat_id) REFERENCES chats(chat_id)
);