CREATE TABLE roles (
  id                       SERIAL PRIMARY KEY,
  `name`                   VARCHAR(150) NOT NULL UNIQUE,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE permissions (
  id                       SERIAL PRIMARY KEY,
  value                    VARCHAR(150) NOT NULL UNIQUE,
  descr                    TEXT
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE roles_to_targets (
  role_id                  BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("account") NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE permissions_to_targets (
  permission_id            BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("account", "role")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (permission_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE tags_to_targets (
  tag_id                   BIGINT UNSIGNED NOT NULL,
  target_type              ENUM("post")  NOT NULL,
  target_id                BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (tag_id, target_type, target_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL,
  user_agent                TINYTEXT,
  os                        TINYTEXT,
  device                    TINYTEXT,
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
  confirm_code              VARCHAR(100),
  password_recovery_code    VARCHAR(100),
  password_recovery_date    BIGINT UNSIGNED
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(255) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   TEXT NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
