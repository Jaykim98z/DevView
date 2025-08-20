-- It's safer to drop existing constraints if they exist, to avoid errors on re-runs
ALTER TABLE interviews DROP CONSTRAINT IF EXISTS interviews_job_position_check;
ALTER TABLE interviews DROP CONSTRAINT IF EXISTS interviews_career_level_check;

-- Update column types to VARCHAR if they aren't already, ensuring consistency
ALTER TABLE interviews ALTER COLUMN job_position TYPE VARCHAR(50);
ALTER TABLE interviews ALTER COLUMN career_level TYPE VARCHAR(50);

-- Add CHECK constraints to mimic Enum behavior and ensure data integrity
ALTER TABLE interviews ADD CONSTRAINT interviews_job_position_check
    CHECK (job_position IN ('BACKEND', 'FRONTEND', 'FULLSTACK', 'DEVOPS', 'DATA_AI'));

ALTER TABLE interviews ADD CONSTRAINT interviews_career_level_check
    CHECK (career_level IN ('JUNIOR', 'MID_LEVEL', 'SENIOR'));