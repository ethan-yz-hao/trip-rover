-- Inserting Roles
INSERT INTO sys_role (role_name, role_key, role_sort, status) VALUES
                                                                  ('Administrator', 'admin', 1, 0),
                                                                  ('User', 'user', 2, 0);

-- Inserting Users
INSERT INTO sys_user (user_name, nick_name, password, type, status, email, phone_number, avatar) VALUES
                                                                                                     ('admin', 'Admin', '$2a$10$NEgAp4igmIoz6iOUTGB34ehY0aHNHblMZOobvmT44Pyq9rVuiwRlS', 1, 0, 'admin@example.com', '1234567890', 'path/to/avatar'),
                                                                                                     ('johndoe', 'John Doe', '$2a$10$NEgAp4igmIoz6iOUTGB34ehY0aHNHblMZOobvmT44Pyq9rVuiwRlS', 0, 0, 'john@example.com', '0987654321', 'path/to/avatar');

-- Inserting Menus
INSERT INTO sys_menu (menu_name, order_num, path, component, visible, status, perms) VALUES
    ('Test', 1, 'system/test/index', 'Test', 0, 0, 'system:test:list');

-- Associating Roles with Menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    ((SELECT id FROM sys_role WHERE role_name = 'Administrator'), (SELECT id FROM sys_menu WHERE menu_name = 'Test'));

-- Associating Users with Roles
INSERT INTO sys_user_role (user_id, role_id) VALUES
    ((SELECT id FROM sys_user WHERE user_name = 'admin'), (SELECT id FROM sys_role WHERE role_name = 'Administrator'));
