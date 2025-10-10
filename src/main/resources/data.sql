-- sql
-- Seed demo posts with explicit UUIDs (PostgreSQL will cast string to UUID)
INSERT INTO posts (id, title, description, post_type, location, contact_name, contact_phone, target_amount, current_amount, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'Need boat evacuation', 'Elderly couple trapped on 2nd floor, water rising.', 'HELP', 'Ward 5, Riverside', 'Lan', '0900000001', NULL, 0, CURRENT_TIMESTAMP);

INSERT INTO posts (id, title, description, post_type, location, contact_name, contact_phone, target_amount, current_amount, created_at)
VALUES ('22222222-2222-2222-2222-222222222222', 'Fundraiser for clean water', 'Raising funds to buy bottled water for 50 families.', 'FUNDRAISE', 'District 3', 'Minh', '0900000002', 10000000, 1500000, CURRENT_TIMESTAMP);