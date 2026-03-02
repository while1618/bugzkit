CREATE TABLE roles (
  role_id   BIGSERIAL    PRIMARY KEY,
  role_name VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_role_name ON roles (role_name);


CREATE TABLE users (
  user_id    BIGSERIAL    PRIMARY KEY,
  username   VARCHAR(255),
  email      VARCHAR(255) NOT NULL,
  password   VARCHAR(255),
  active     BOOLEAN      NOT NULL DEFAULT FALSE,
  lock       BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX idx_username       ON users (username);
CREATE UNIQUE INDEX idx_email          ON users (email);
CREATE        INDEX idx_username_email ON users (username, email);


CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (user_id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
);


CREATE TABLE devices (
  id             BIGSERIAL    PRIMARY KEY,
  user_id        BIGINT       NOT NULL,
  device_id      VARCHAR(255) NOT NULL,
  user_agent     TEXT,
  created_at     TIMESTAMP    NOT NULL,
  last_active_at TIMESTAMP    NOT NULL,
  CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE        INDEX idx_device_user_id     ON devices (user_id);
CREATE UNIQUE INDEX idx_device_user_device ON devices (user_id, device_id);
