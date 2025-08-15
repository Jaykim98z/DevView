CREATE INDEX IF NOT EXISTS idx_posts_user           ON community_posts USING btree (user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at     ON community_posts USING btree (created_at);
CREATE INDEX IF NOT EXISTS idx_posts_category       ON community_posts USING btree (category);
CREATE INDEX IF NOT EXISTS idx_posts_grade          ON community_posts USING btree (grade);
CREATE INDEX IF NOT EXISTS idx_posts_interview_type ON community_posts USING btree (interview_type);
CREATE INDEX IF NOT EXISTS idx_posts_deleted        ON community_posts USING btree (deleted);