BEGIN;

-- 1) 컬럼이 없으면 추가 (타입만 우선)
ALTER TABLE community_posts
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN;

-- 2) NULL 값은 false로 채움 (기존 데이터 보호)
UPDATE community_posts
SET deleted = FALSE
WHERE deleted IS NULL;

-- 3) 기본값과 NOT NULL 제약을 "각각" 설정
ALTER TABLE community_posts
    ALTER COLUMN deleted SET DEFAULT FALSE;

ALTER TABLE community_posts
    ALTER COLUMN deleted SET NOT NULL;

COMMIT;
