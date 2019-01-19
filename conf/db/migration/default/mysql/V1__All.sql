CREATE TABLE roles (
  account_id                BIGINT UNSIGNED NOT NULL,
  role_name                 ENUM('client', 'editor', 'writer', 'admin') NOT NULL,
  PRIMARY KEY (account_id, role_name)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL, 
  session_key               VARCHAR(100) NOT NULL UNIQUE,
  created                   BIGINT UNSIGNED NOT NULL,
  expire                    BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE accounts (
  id                        SERIAL PRIMARY KEY,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  confirmation_status       ENUM('confirmed', 'wait confirmation') NOT NULL,
  account_status            ENUM('normal', 'locked') NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL,
  confirm_code              VARCHAR(100)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(255) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   TEXT NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE categories (
  id                        SERIAL PRIMARY KEY,
  parent_id                 BIGINT UNSIGNED,
  name                      VARCHAR(255) NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE posts (
  id                        SERIAL PRIMARY KEY,
  category_id               BIGINT UNSIGNED,
  owner_id                  BIGINT UNSIGNED NOT NULL,
  title                     VARCHAR(255) NOT NULL,
  thumbnail                 VARCHAR(255),
  content                   TEXT NOT NULL,
  status                    ENUM('draft', 'sandbox', 'published') NOT NULL,
  created                   BIGINT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE menus (
  id                        SERIAL PRIMARY KEY,
  parent_id                 BIGINT UNSIGNED,
  menu_id                   BIGINT UNSIGNED,
  link                      VARCHAR(255),
  name                      VARCHAR(255) NOT NULL,
  content                   TEXT,
  `order`                   INT UNSIGNED NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO options VALUES (1,'REGISTER_ALLOWED','Registration allowance','Boolean','true');
INSERT INTO options VALUES (2,'POSTS_CHANGE_ALLOWED', 'Posts change allowance', 'Boolean', 'true');
INSERT INTO options VALUES (3,'POSTS_CREATE_ALLOWED', 'Posts change allowance', 'Boolean', 'true');
INSERT INTO options VALUES (4,'AFTER_PAGE_SCRIPT', 'After page script', 'String', '<!-- -->');
INSERT INTO options VALUES (5,'MAIN_MENU_ID', 'Main menu id', 'Option[Int]', '');
INSERT INTO options VALUES (6,'INDEX_PAGE_ID', 'Main page', 'Option[Long]', '');





