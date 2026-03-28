INSERT INTO roles (role_name) VALUES ('USER'), ('ADMIN');

INSERT INTO users (username, email, password, active, lock, created_at) VALUES
  ('admin',        'admin@localhost',        '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('user',         'user@localhost',         '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('deactivated1', 'deactivated1@localhost', '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', false, false, NOW()),
  ('deactivated2', 'deactivated2@localhost', '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', false, false, NOW()),
  ('deactivated3', 'deactivated3@localhost', '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', false, false, NOW()),
  ('locked',       'locked@localhost',       '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  true,  NOW()),
  ('update1',      'update1@localhost',      '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('update2',      'update2@localhost',      '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('update3',      'update3@localhost',      '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('update4',      'update4@localhost',      '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('delete1',      'delete1@localhost',      '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW()),
  ('delete2',      'delete2@localhost',      '$2a$10$4Nm6TI1IULsNWuBSLXp85uK5tp5pbfjk1UMByB2.zSoQPmKsgpWl2', true,  false, NOW());

INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 1), (1, 2),
  (2, 1),
  (3, 1),
  (4, 1),
  (5, 1),
  (6, 1),
  (7, 1),
  (8, 1),
  (9, 1),
  (10, 1),
  (11, 1),
  (12, 1);
