INSERT INTO training_types (type) VALUES ('CARDIO') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('STRENGTH') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('FLEXIBILITY') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('HIIT') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('BALANCE') ON CONFLICT (type) DO NOTHING;

INSERT INTO users (first_name, last_name, username, password, is_active, role)
VALUES ('felipe', 'munevar', 'felipe.munevar', '$2a$12$bQeaN0p6WSVlnwzGM60f8..Do3xzdAXKTO1GG7eDFPuqBDj13XFHu', true, 'ADMIN')
ON CONFLICT (username) DO NOTHING;