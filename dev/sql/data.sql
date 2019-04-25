INSERT INTO options VALUES(1, "REGISTER_ALLOWED", "Registration allowance", "Boolean", "true");
INSERT INTO options VALUES(2, "AFTER_PAGE_SCRIPT", "After page script", "String", "<!-- -->");

INSERT INTO roles VALUES(1, "admin", "Administrator");
INSERT INTO roles VALUES(2, "client", "Client");

INSERT INTO permissions VALUES(1, "ref.admin", "Reference admin permission");
INSERT INTO permissions VALUES(2, "ref.client", "Reference client permission");

INSERT INTO permissions_to_targets VALUES(1, "role", 1);
INSERT INTO permissions_to_targets VALUES(2, "role", 1);
INSERT INTO permissions_to_targets VALUES(2, "role", 2);











