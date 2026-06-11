USE tarkov_community;

INSERT INTO sys_user (username, password, nickname, email, role, status, contribution)
VALUES (
  'admin',
  '$2a$10$fZQPznyJUYHn6Vr3DhaEYez/xqqLYYQyR/xqF1FaD0Gimpd0Rudgm',
  '社区管理员',
  'admin@example.com',
  'ADMIN',
  'NORMAL',
  100
)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  nickname = VALUES(nickname),
  role = 'ADMIN',
  status = 'NORMAL',
  contribution = GREATEST(contribution, VALUES(contribution));
