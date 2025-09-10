INSERT INTO training_types (type) VALUES ('CARDIO') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('STRENGTH') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('FLEXIBILITY') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('HIIT') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('BALANCE') ON CONFLICT (type) DO NOTHING;

INSERT INTO users (first_name, last_name, username, password, is_active, role)
VALUES ('felipe', 'munevar', 'felipe.munevar', '$2a$12$bQeaN0p6WSVlnwzGM60f8..Do3xzdAXKTO1GG7eDFPuqBDj13XFHu', true, 'ADMIN')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (first_name, last_name, username, password, is_active, role)
VALUES ('eimer', 'castro', 'eimer.castro', '$2a$12$bQeaN0p6WSVlnwzGM60f8..Do3xzdAXKTO1GG7eDFPuqBDj13XFHu', true, 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (id, first_name, last_name, username, password, is_active, role)
VALUES (10, 'maria', 'ramirez', 'maria.ramirez', '$2a$12$bQeaN0p6WSVlnwzGM60f8..Do3xzdAXKTO1GG7eDFPuqBDj13XFHu', true, 'TRAINER')
ON CONFLICT (id) DO NOTHING;
INSERT INTO trainers (id, training_type_id, user_id)
VALUES (1, 3, 10)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, first_name, last_name, username, password, is_active, role)
VALUES (20, 'antonia', 'neira', 'antonia.neira', '$2a$12$bQeaN0p6WSVlnwzGM60f8..Do3xzdAXKTO1GG7eDFPuqBDj13XFHu', true, 'TRAINEE')
ON CONFLICT (id) DO NOTHING;
INSERT INTO trainees (id, address, date_of_birth, user_id)
VALUES (1, 'av siempre viva 123', '2015-06-24', 20)
ON CONFLICT (id) DO NOTHING;