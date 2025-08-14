ALTER TABLE community_posts
ADD COLUMN IF NOT EXISTS deleted boolean NOT NULL DEFAULT false;

--조회 필터 성능 개선
CREATE INDEX IF NOT EXISTS idx_community_posts_deleted
ON community_posts (deleted);