ALTER TABLE interviews ADD COLUMN IF NOT EXISTS session_mode VARCHAR(20) NOT NULL DEFAULT 'PRACTICE';
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS interview_mode VARCHAR(30) NOT NULL DEFAULT 'STANDARD';
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS current_question_started_at TIMESTAMPTZ;

UPDATE interviews SET session_mode = 'PRACTICE' WHERE session_mode IS NULL;
UPDATE interviews SET interview_mode = 'STANDARD' WHERE interview_mode IS NULL;

ALTER TABLE interviews DROP CONSTRAINT IF EXISTS chk_interviews_session_mode;
ALTER TABLE interviews ADD CONSTRAINT chk_interviews_session_mode
    CHECK (session_mode IN ('PRACTICE', 'EXAM'));

ALTER TABLE interviews DROP CONSTRAINT IF EXISTS chk_interviews_interview_mode;
ALTER TABLE interviews ADD CONSTRAINT chk_interviews_interview_mode
    CHECK (interview_mode IN ('STANDARD', 'JOB_POSTING', 'PORTFOLIO'));
