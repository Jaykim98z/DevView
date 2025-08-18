-- Add the new column
ALTER TABLE interviews ADD COLUMN question_count INT NOT NULL DEFAULT 5;