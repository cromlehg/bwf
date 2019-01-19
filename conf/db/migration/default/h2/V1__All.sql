CREATE TABLE roles (
  account_id                BIGINT UNSIGNED NOT NULL,
  role_name                 VARCHAR(100) NOT NULL,
  PRIMARY KEY (account_id, role_name)
);

CREATE TABLE sessions (
  id                        SERIAL PRIMARY KEY,
  user_id                   BIGINT UNSIGNED NOT NULL,
  ip                        VARCHAR(100) NOT NULL, 
  session_key               VARCHAR(100) NOT NULL UNIQUE,
  created                   BIGINT UNSIGNED NOT NULL,
  expire                    BIGINT UNSIGNED NOT NULL
);

CREATE TABLE accounts (
  id                        SERIAL PRIMARY KEY,
  login                     VARCHAR(100) NOT NULL UNIQUE,
  email                     VARCHAR(100) NOT NULL UNIQUE,
  hash                      VARCHAR(60),
  confirmation_status       VARCHAR(100) NOT NULL,
  account_status            VARCHAR(100) NOT NULL,
  registered                BIGINT UNSIGNED NOT NULL,
  confirm_code              VARCHAR(100)
);

CREATE TABLE options (
  id                        SERIAL PRIMARY KEY,
  name                      VARCHAR(100) NOT NULL,
  descr                     VARCHAR(255) NOT NULL,
  `type`                    VARCHAR(100) NOT NULL,
  `value`                   TEXT NOT NULL
);

CREATE TABLE categories (
  id                        SERIAL PRIMARY KEY,
  parent_id                 BIGINT UNSIGNED,
  name                      VARCHAR(255) NOT NULL
);

CREATE TABLE posts (
  id                        SERIAL PRIMARY KEY,
  category_id               BIGINT UNSIGNED,
  owner_id                  BIGINT UNSIGNED NOT NULL,
  title                     VARCHAR(255) NOT NULL,
  thumbnail                 VARCHAR(255),
  content                   TEXT NOT NULL,
  status                    VARCHAR(100) NOT NULL,
  created                   BIGINT UNSIGNED NOT NULL
);

CREATE TABLE menus (
  id                        SERIAL PRIMARY KEY,
  parent_id                 BIGINT UNSIGNED,
  menu_id                   BIGINT UNSIGNED,
  link                      VARCHAR(255),
  name                      VARCHAR(255) NOT NULL,
  content                   TEXT,
  `order`                   INT UNSIGNED NOT NULL
);


INSERT INTO accounts VALUES (1,'testadmin','testadmin@project.country','$2a$10$EwrXfFADQmgbfyY54fPMbuWCnmTSbCpl9Rfrkc0.3OrVp/GeBMTp6','confirmed','normal',1529936034487,NULL);
INSERT INTO accounts VALUES (2,'testclient','testclient@project.country','$2a$10$EwrXfFADQmgbfyY54fPMbuWCnmTSbCpl9Rfrkc0.3OrVp/GeBMTp6','confirmed','normal',1529936034487,NULL);

INSERT INTO roles VALUES (1,'client');
INSERT INTO roles VALUES (1,'admin');
INSERT INTO roles VALUES (2,'client');

INSERT INTO categories VALUES (1,NULL,'MENU');

INSERT INTO posts VALUES (1,1,1,'about',NULL,'<p>Page about us</p>','published',1532683488456);
INSERT INTO posts VALUES (2,1,1,'contacts',NULL,'<p>Contacts page</p>','published',1532683506098);
INSERT INTO posts VALUES (3,1,1,'news',NULL,'<p>News page</p>','published',1532683552072);
INSERT INTO posts VALUES (4,1,1,'story',NULL,'<p>Story page</p>','published',1532683564333);
INSERT INTO posts VALUES (5,1,1,'products',NULL,'<p>products page</p>','published',1532683488456);

INSERT INTO menus VALUES (1, NULL, 1, NULL, 'Main menu', NULL, 0);
INSERT INTO menus VALUES (2, 1, 1, '/app/pages/page/5', 'products', NULL, 1);
INSERT INTO menus VALUES (3, 1, 1, NULL, 'company', NULL, 2);
INSERT INTO menus VALUES (4, 2, 1, '/app/pages/page/3', 'news', NULL, 1);
INSERT INTO menus VALUES (5, 2, 1, '/app/pages/page/4', 'story', NULL, 2);
INSERT INTO menus VALUES (6, 1, 1, '/app/pages/page/1', 'about', NULL, 2);
INSERT INTO menus VALUES (7, 1, 1, '/app/pages/page/2', 'contacts', NULL, 3);

INSERT INTO options VALUES (1,'REGISTER_ALLOWED','Registration allowance','Boolean','true');
INSERT INTO options VALUES (2,'POSTS_CHANGE_ALLOWED', 'Posts change allowance', 'Boolean', 'true');
INSERT INTO options VALUES (3,'POSTS_CREATE_ALLOWED', 'Posts change allowance', 'Boolean', 'true');
INSERT INTO options VALUES (4,'AFTER_PAGE_SCRIPT', 'After page script', 'String', '<!-- -->');
INSERT INTO options VALUES (5,'MAIN_MENU_ID', 'Main menu id', 'Option[Int]', '1');
INSERT INTO options VALUES (6,'INDEX_PAGE_ID', 'Main page', 'Option[Long]', '1');



