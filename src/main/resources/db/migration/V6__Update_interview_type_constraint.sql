-- First, remove the old constraint
ALTER TABLE interviews DROP CONSTRAINT interviews_interview_type_check;

-- Then, add the new, updated constraint with all valid types
ALTER TABLE interviews ADD CONSTRAINT interviews_interview_type_check
CHECK (interview_type IN ('TECHNICAL', 'PRACTICAL', 'BEHAVIORAL', 'COMPREHENSIVE'));