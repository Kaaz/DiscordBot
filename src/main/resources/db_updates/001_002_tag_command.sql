CREATE TABLE tags
         (
         id INT PRIMARY KEY AUTO_INCREMENT,
         tag_name VARCHAR(32),
         guild_id INT,
         response TEXT
         );
ALTER TABLE tags ADD user_id INT NULL;
ALTER TABLE tags ADD creation_date TIMESTAMP NULL;