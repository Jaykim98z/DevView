-- 공백값 정리
UPDATE community_posts SET category = NULL WHERE category IS NOT NULL AND btrim(category) = '';
UPDATE community_posts SET level    = NULL WHERE level    IS NOT NULL AND btrim(level)    = '';

-- level 표준화
UPDATE community_posts SET level = 'MID_LEVEL'
WHERE level IS NOT NULL AND regexp_replace(level, '\s+', '', 'g') ILIKE 'mid';

UPDATE community_posts SET level = 'JUNIOR'
WHERE level IS NOT NULL AND level ILIKE '주니어';

UPDATE community_posts SET level = 'SENIOR'
WHERE level IS NOT NULL AND level ILIKE '시니어';

-- category 표준화
UPDATE community_posts SET category = 'BACKEND'
WHERE category IS NOT NULL AND (category ILIKE '백엔드' OR upper(btrim(category)) = 'BACKEND');

UPDATE community_posts SET category = 'FRONTEND'
WHERE category IS NOT NULL AND (category ILIKE '프론트엔드' OR upper(btrim(category)) = 'FRONTEND');

UPDATE community_posts SET category = 'FULLSTACK'
WHERE category IS NOT NULL AND (category ILIKE '풀스택' OR upper(btrim(category)) = 'FULLSTACK');

UPDATE community_posts SET category = 'DEVOPS'
WHERE category IS NOT NULL AND (category ILIKE '데브옵스' OR upper(btrim(category)) = 'DEVOPS');

UPDATE community_posts SET category = 'DATA_AI'
WHERE category IS NOT NULL AND (
    upper(btrim(category)) IN ('DATA_AI','DATA/AI','DATA','AI')
    OR category ILIKE '데이터'
    OR category ILIKE '에이아이'
);

-- interviews 기준 category 백필
UPDATE community_posts p
SET category =
  CASE upper(i.job_position)
    WHEN 'BACKEND'     THEN 'BACKEND'
    WHEN 'FRONTEND'    THEN 'FRONTEND'
    WHEN 'FULLSTACK'   THEN 'FULLSTACK'
    WHEN 'DEVOPS'      THEN 'DEVOPS'
    WHEN 'DATA_AI'     THEN 'DATA_AI'
    WHEN 'DATA/AI'     THEN 'DATA_AI'
    WHEN '데브옵스'    THEN 'DEVOPS'
    WHEN '백엔드'      THEN 'BACKEND'
    WHEN '프론트엔드'  THEN 'FRONTEND'
    WHEN '풀스택'      THEN 'FULLSTACK'
    ELSE upper(i.job_position)
  END
FROM interview_results r
JOIN interviews i ON i.interview_id = r.interview_id
WHERE p.interview_result_id = r.result_id
  AND p.category IS NULL;

-- interviews 기준 level 백필
UPDATE community_posts p
SET level =
  CASE upper(i.career_level)
    WHEN 'MID'         THEN 'MID_LEVEL'
    WHEN 'MIDLEVEL'    THEN 'MID_LEVEL'
    WHEN 'MID_LEVEL'   THEN 'MID_LEVEL'
    WHEN 'JUNIOR'      THEN 'JUNIOR'
    WHEN 'SENIOR'      THEN 'SENIOR'
    WHEN '주니어'      THEN 'JUNIOR'
    WHEN '시니어'      THEN 'SENIOR'
    ELSE upper(i.career_level)
  END
FROM interview_results r
JOIN interviews i ON i.interview_id = r.interview_id
WHERE p.interview_result_id = r.result_id
  AND p.level IS NULL;

-- 최종 정규화 재확인(백필 후 잔여값 정리)
UPDATE community_posts SET level = 'MID_LEVEL'
WHERE level IS NOT NULL AND upper(btrim(level)) IN ('MID','MIDLEVEL');

UPDATE community_posts SET level = 'JUNIOR'
WHERE level IS NOT NULL AND level ILIKE '주니어';

UPDATE community_posts SET level = 'SENIOR'
WHERE level IS NOT NULL AND level ILIKE '시니어';

UPDATE community_posts SET category = 'DATA_AI'
WHERE category IS NOT NULL AND upper(btrim(category)) IN ('DATA/AI','DATA','AI');
