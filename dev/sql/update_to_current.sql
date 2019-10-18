ALTER TABLE posts ADD COLUMN meta_title VARCHAR(255);
ALTER TABLE posts ADD COLUMN meta_descr VARCHAR(255);
ALTER TABLE posts ADD COLUMN meta_keywords VARCHAR(255);
ALTER TABLE posts ADD COLUMN post_type ENUM('page', 'article') NOT NULL DEFAULT 'article';
update posts set post_type = "page" where id in (38, 35, 33, 23, 21, 20, 19, 14);
insert into permissions_to_targets values(7, "role", 1);
insert into permissions_to_targets values(8, "role", 1);
insert into permissions_to_targets values(7, "role", 2);
