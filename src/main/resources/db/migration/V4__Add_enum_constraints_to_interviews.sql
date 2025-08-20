-- Update column types to VARCHAR if they aren't already, ensuring consistency
ALTER TABLE interviews ALTER COLUMN job_position TYPE VARCHAR(50);
ALTER TABLE interviews ALTER COLUMN career_level TYPE VARCHAR(50);

-- Add CHECK constraints to mimic Enum behavior and ensure data integrity
ALTER TABLE interviews ADD CONSTRAINT interviews_job_position_check
    CHECK (job_position IN ('BACKEND', 'FRONTEND', 'FULLSTACK', 'DEVOPS', 'DATA_AI'));

ALTER TABLE interviews ADD CONSTRAINT interviews_career_level_check
    CHECK (career_level IN ('JUNIOR', 'MID_LEVEL', 'SENIOR'));