--MERGE INTO training_types (type) KEY (type) VALUES ('CARDIO');
--MERGE INTO training_types (type) KEY (type) VALUES ('STRENGTH');
--MERGE INTO training_types (type) KEY (type) VALUES ('FLEXIBILITY');
--MERGE INTO training_types (type) KEY (type) VALUES ('HIIT');
INSERT INTO training_types (type) VALUES ('CARDIO') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('STRENGTH') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('FLEXIBILITY') ON CONFLICT (type) DO NOTHING;
INSERT INTO training_types (type) VALUES ('HIIT') ON CONFLICT (type) DO NOTHING;