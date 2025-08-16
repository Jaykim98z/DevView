-- 1) deleted 컬럼이 없으면 추가
ALTER TABLE community_posts
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN;

-- 2) NULL -> false 보정
UPDATE community_posts
SET deleted = FALSE
WHERE deleted IS NULL;

-- 3) 기본값/NOT NULL 설정 (각각 분리)
ALTER TABLE community_posts
    ALTER COLUMN deleted SET DEFAULT FALSE;

ALTER TABLE community_posts
    ALTER COLUMN deleted SET NOT NULL;

-- 4) 카운트/점수 컬럼 보정 (NULL -> 0, 기본값/NOT NULL)
UPDATE community_posts SET like_count = 0 WHERE like_count IS NULL;
UPDATE community_posts SET scrap_count = 0 WHERE scrap_count IS NULL;
UPDATE community_posts SET view_count  = 0 WHERE view_count  IS NULL;
UPDATE community_posts SET score       = 0 WHERE score       IS NULL;

ALTER TABLE community_posts
    ALTER COLUMN like_count SET DEFAULT 0,
    ALTER COLUMN scrap_count SET DEFAULT 0,
    ALTER COLUMN view_count  SET DEFAULT 0,
    ALTER COLUMN score       SET DEFAULT 0;

ALTER TABLE community_posts
    ALTER COLUMN like_count SET NOT NULL,
    ALTER COLUMN scrap_count SET NOT NULL,
    ALTER COLUMN view_count  SET NOT NULL,
    ALTER COLUMN score       SET NOT NULL;

-- 5) created_at 기본값 now()
ALTER TABLE community_posts
    ALTER COLUMN created_at SET DEFAULT NOW();

-- 6) 제목/내용 NOT NULL (데이터가 NULL이면 먼저 값 보정 필요)
ALTER TABLE community_posts
    ALTER COLUMN title   SET NOT NULL,
    ALTER COLUMN content SET NOT NULL;
