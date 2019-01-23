INSERT INTO options VALUES(1, "REGISTER_ALLOWED", "Registration allowance", "Boolean", "true");
INSERT INTO options VALUES(2, "POSTS_CHANGE_ALLOWED", "Posts change allowance", "Boolean", "true");
INSERT INTO options VALUES(3, "POSTS_CREATE_ALLOWED", "Posts create allowance", "Boolean", "true");
INSERT INTO options VALUES(4, "AFTER_PAGE_SCRIPT", "After page script", "String", "<!-- -->");
INSERT INTO options VALUES(5, "MAIN_MENU_ID", "Main menu id", "Option[Int]", "");
INSERT INTO options VALUES(6, "INDEX_PAGE_ID", "Main page", "Option[Long]", "");
INSERT INTO options VALUES(7, "SHORTCODES_POST_DEPTH_LIMIT", "Post shortcode handler depth limit", "Option[Long]", "5");
INSERT INTO options VALUES(8, "SHORTCODES_POST_WIDE_LIMIT", "Post shortcode handler wide limit", "Option[Long]", "5");

# permissions and roles
INSERT INTO roles VALUES(1, "admin", "Administrator");
INSERT INTO roles VALUES(2, "editor", "Editor");
INSERT INTO roles VALUES(3, "writer", "Articles writes");
INSERT INTO roles VALUES(4, "client", "Platform client");

INSERT INTO permissions VALUES(1, "posts.create.conditional", "Post conditional create permission");
INSERT INTO permissions VALUES(2, "posts.create.anytime", "Post create anytime permission");
INSERT INTO permissions VALUES(3, "posts.own.edit.anytime", "Post owner anytime edit permission");
INSERT INTO permissions VALUES(4, "posts.any.edit.anytime", "Any post anytime edit permission");
INSERT INTO permissions VALUES(5, "posts.own.edit.conditional", "Post owner conditional edit permission");
INSERT INTO permissions VALUES(6, "posts.any.edit.conditional", "Conditional edit any posts permission");
INSERT INTO permissions VALUES(7, "posts.own.remove", "Post owner posts permission");
INSERT INTO permissions VALUES(8, "posts.any.remove", "Remove any posts permission");
INSERT INTO permissions VALUES(9, "posts.open.view", "Oppened posts view permission");
INSERT INTO permissions VALUES(10, "posts.any.view", "Any posts view permission");
INSERT INTO permissions VALUES(11, "posts.own.list.view", "View own posts list in lk permission");
INSERT INTO permissions VALUES(12, "posts.any.list.view", "View all posts list in lk permission");
INSERT INTO permissions VALUES(13, "options.list.view", "View options list permission");
INSERT INTO permissions VALUES(14, "accounts.list.view", "View accounts list permission");
INSERT INTO permissions VALUES(15, "accounts.any.edit", "Edit any accounts permission");
INSERT INTO permissions VALUES(16, "permissions.any.edit", "Change permissions and roles anytime");
INSERT INTO permissions VALUES(17, "options.edit", "Edit options permission");
INSERT INTO permissions VALUES(18, "menu.view", "View menu permission");
INSERT INTO permissions VALUES(19, "comments.any.edit", "All permisssions for all comments in admin panel");
INSERT INTO permissions VALUES(20, "comments.own.edit", "All permissions for own comments in admin panel");
INSERT INTO permissions VALUES(21, "comments.create.conditional", "Create comments permission any time");
INSERT INTO permissions VALUES(22, "comments.create.anytime", "Conditionaly create comments");


INSERT INTO permissions_to_targets VALUES(2, "role", 1);
INSERT INTO permissions_to_targets VALUES(4, "role", 1);
INSERT INTO permissions_to_targets VALUES(10, "role", 1);
INSERT INTO permissions_to_targets VALUES(11, "role", 1);
INSERT INTO permissions_to_targets VALUES(12, "role", 1);
INSERT INTO permissions_to_targets VALUES(13, "role", 1);
INSERT INTO permissions_to_targets VALUES(14, "role", 1);
INSERT INTO permissions_to_targets VALUES(15, "role", 1);
INSERT INTO permissions_to_targets VALUES(16, "role", 1);
INSERT INTO permissions_to_targets VALUES(17, "role", 1);
INSERT INTO permissions_to_targets VALUES(18, "role", 1);
INSERT INTO permissions_to_targets VALUES(19, "role", 1);
INSERT INTO permissions_to_targets VALUES(22, "role", 1);

INSERT INTO permissions_to_targets VALUES(1, "role", 2);
INSERT INTO permissions_to_targets VALUES(6, "role", 2);
INSERT INTO permissions_to_targets VALUES(9, "role", 2);
INSERT INTO permissions_to_targets VALUES(11, "role", 2);
INSERT INTO permissions_to_targets VALUES(12, "role", 2);
INSERT INTO permissions_to_targets VALUES(19, "role", 2);
INSERT INTO permissions_to_targets VALUES(21, "role", 2);

INSERT INTO permissions_to_targets VALUES(1, "role", 3);
INSERT INTO permissions_to_targets VALUES(5, "role", 3);
INSERT INTO permissions_to_targets VALUES(9, "role", 3);
INSERT INTO permissions_to_targets VALUES(11, "role", 3);
INSERT INTO permissions_to_targets VALUES(20, "role", 3);
INSERT INTO permissions_to_targets VALUES(21, "role", 3);

INSERT INTO permissions_to_targets VALUES(9, "role", 4);
INSERT INTO permissions_to_targets VALUES(21, "role", 4);


# Only for debug - should remove after test
INSERT INTO roles_to_targets VALUES(1, "account", 1);
INSERT INTO roles_to_targets VALUES(4, "account", 2);
INSERT INTO roles_to_targets VALUES(4, "account", 3);
INSERT INTO roles_to_targets VALUES(4, "account", 4);
INSERT INTO roles_to_targets VALUES(4, "account", 5);
INSERT INTO roles_to_targets VALUES(4, "account", 6);
INSERT INTO roles_to_targets VALUES(4, "account", 7);
INSERT INTO roles_to_targets VALUES(4, "account", 8);
INSERT INTO roles_to_targets VALUES(4, "account", 9);
INSERT INTO roles_to_targets VALUES(4, "account", 10);
INSERT INTO roles_to_targets VALUES(4, "account", 12);
INSERT INTO roles_to_targets VALUES(2, "account", 14);
INSERT INTO roles_to_targets VALUES(4, "account", 19);
INSERT INTO roles_to_targets VALUES(4, "account", 20);
INSERT INTO roles_to_targets VALUES(4, "account", 21);
INSERT INTO roles_to_targets VALUES(4, "account", 22);
INSERT INTO roles_to_targets VALUES(4, "account", 23);
INSERT INTO roles_to_targets VALUES(4, "account", 24);












