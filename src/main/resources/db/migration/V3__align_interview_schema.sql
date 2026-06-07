-- Align interview tables with current JPA entities (job_role, question_order, content, etc.)

-- interviews
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS job_role VARCHAR(100);
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS tech_stack TEXT[];
ALTER TABLE interviews ADD COLUMN IF NOT EXISTS experience_level VARCHAR(50);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'interviews' AND column_name = 'job_position'
    ) THEN
        UPDATE interviews SET job_role = job_position WHERE job_role IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'interviews' AND column_name = 'career_level'
    ) THEN
        UPDATE interviews SET experience_level = career_level WHERE experience_level IS NULL;
    END IF;
END $$;

UPDATE interviews SET job_role = '미지정' WHERE job_role IS NULL;
UPDATE interviews SET experience_level = '신입' WHERE experience_level IS NULL;

ALTER TABLE interviews DROP CONSTRAINT IF EXISTS chk_interviews_type;
ALTER TABLE interviews DROP CONSTRAINT IF EXISTS chk_interviews_career;

ALTER TABLE interviews DROP COLUMN IF EXISTS interview_type;
ALTER TABLE interviews DROP COLUMN IF EXISTS career_level;
ALTER TABLE interviews DROP COLUMN IF EXISTS company_name;
ALTER TABLE interviews DROP COLUMN IF EXISTS job_position;

ALTER TABLE interviews ALTER COLUMN job_role SET NOT NULL;
ALTER TABLE interviews ALTER COLUMN experience_level SET NOT NULL;

ALTER TABLE interviews DROP CONSTRAINT IF EXISTS chk_interviews_status;
ALTER TABLE interviews ADD CONSTRAINT chk_interviews_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED'));

-- interview_questions
ALTER TABLE interview_questions ADD COLUMN IF NOT EXISTS question_order INTEGER;
ALTER TABLE interview_questions ADD COLUMN IF NOT EXISTS content TEXT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'interview_questions' AND column_name = 'question_sequence'
    ) THEN
        UPDATE interview_questions SET question_order = question_sequence WHERE question_order IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'interview_questions' AND column_name = 'question'
    ) THEN
        UPDATE interview_questions SET content = question WHERE content IS NULL;
    END IF;
END $$;

ALTER TABLE interview_questions DROP CONSTRAINT IF EXISTS uk_interview_questions_interview_sequence;
ALTER TABLE interview_questions DROP COLUMN IF EXISTS question_sequence;
ALTER TABLE interview_questions DROP COLUMN IF EXISTS question;

ALTER TABLE interview_questions ALTER COLUMN question_order SET NOT NULL;
ALTER TABLE interview_questions ALTER COLUMN content SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_interview_questions_interview_order
    ON interview_questions (interview_id, question_order);

-- interview_answers
ALTER TABLE interview_answers ADD COLUMN IF NOT EXISTS interview_id BIGINT;
ALTER TABLE interview_answers ADD COLUMN IF NOT EXISTS content TEXT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'interview_answers' AND column_name = 'answer'
    ) THEN
        UPDATE interview_answers SET content = answer WHERE content IS NULL;
    END IF;
END $$;

UPDATE interview_answers ia
SET interview_id = iq.interview_id
FROM interview_questions iq
WHERE ia.question_id = iq.id AND ia.interview_id IS NULL;

ALTER TABLE interview_answers DROP COLUMN IF EXISTS answer;
ALTER TABLE interview_answers DROP COLUMN IF EXISTS submitted_at;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM interview_answers WHERE interview_id IS NULL
    ) THEN
        ALTER TABLE interview_answers ALTER COLUMN interview_id SET NOT NULL;
        ALTER TABLE interview_answers ALTER COLUMN content SET NOT NULL;
    END IF;
END $$;

ALTER TABLE interview_answers DROP CONSTRAINT IF EXISTS fk_interview_answers_interview;
ALTER TABLE interview_answers
    ADD CONSTRAINT fk_interview_answers_interview FOREIGN KEY (interview_id) REFERENCES interviews(id);

-- interview_results
ALTER TABLE interview_results ADD COLUMN IF NOT EXISTS question_id BIGINT;
ALTER TABLE interview_results ADD COLUMN IF NOT EXISTS answer_id BIGINT;
ALTER TABLE interview_results ADD COLUMN IF NOT EXISTS score INTEGER;

ALTER TABLE interview_results DROP CONSTRAINT IF EXISTS uk_interview_results_interview_id;
ALTER TABLE interview_results DROP CONSTRAINT IF EXISTS chk_interview_results_grade;

ALTER TABLE interview_results DROP COLUMN IF EXISTS grade;
ALTER TABLE interview_results DROP COLUMN IF EXISTS overall_feedback;
ALTER TABLE interview_results DROP COLUMN IF EXISTS evaluated_at;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_interview_results_question'
    ) THEN
        ALTER TABLE interview_results
            ADD CONSTRAINT fk_interview_results_question FOREIGN KEY (question_id) REFERENCES interview_questions(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_interview_results_answer'
    ) THEN
        ALTER TABLE interview_results
            ADD CONSTRAINT fk_interview_results_answer FOREIGN KEY (answer_id) REFERENCES interview_answers(id);
    END IF;
END $$;
