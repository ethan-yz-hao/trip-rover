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
    ('Test', 1, '/*', 'Test', 0, 0, 'user:all');

-- Associating Roles with Menus
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
    ((SELECT id FROM sys_role WHERE role_name = 'Administrator'), (SELECT id FROM sys_menu WHERE menu_name = 'Test')),
    ((SELECT id FROM sys_role WHERE role_name = 'User'), (SELECT id FROM sys_menu WHERE menu_name = 'Test'));

-- Associating Users with Roles
INSERT INTO sys_user_role (user_id, role_id) VALUES
    ((SELECT id FROM sys_user WHERE user_name = 'admin'), (SELECT id FROM sys_role WHERE role_name = 'Administrator')),
    ((SELECT id FROM sys_user WHERE user_name = 'johndoe'), (SELECT id FROM sys_role WHERE role_name = 'User'));

-- Inserting Plans
INSERT INTO plans (plan_name, version) VALUES
                                           ('Trip to New York', 1),
                                           ('Road Trip across California', 1);

-- Inserting PlanPlaces
INSERT INTO plan_places (plan_id, sequence_number, place_id) VALUES
                                                                 ((SELECT plan_id FROM plans WHERE plan_name = 'Trip to New York'), 0, 'NYC001'),
                                                                 ((SELECT plan_id FROM plans WHERE plan_name = 'Trip to New York'), 1, 'NYC002'),
                                                                 ((SELECT plan_id FROM plans WHERE plan_name = 'Trip to New York'), 2, 'NYC003'),
                                                                 ((SELECT plan_id FROM plans WHERE plan_name = 'Road Trip across California'), 0, 'CA001'),
                                                                 ((SELECT plan_id FROM plans WHERE plan_name = 'Road Trip across California'), 1, 'CA002'),
                                                                 ((SELECT plan_id FROM plans WHERE plan_name = 'Road Trip across California'), 2, 'CA003');

-- Associating Users with Plans
INSERT INTO user_plans (plan_id, user_id) VALUES
                                              ((SELECT plan_id FROM plans WHERE plan_name = 'Trip to New York'), (SELECT id FROM sys_user WHERE user_name = 'admin')),
                                              ((SELECT plan_id FROM plans WHERE plan_name = 'Road Trip across California'), (SELECT id FROM sys_user WHERE user_name = 'admin')),
                                              ((SELECT plan_id FROM plans WHERE plan_name = 'Road Trip across California'), (SELECT id FROM sys_user WHERE user_name = 'johndoe'));
