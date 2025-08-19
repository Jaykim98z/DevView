-- DevView 프로젝트 완전한 초기 데이터베이스 스키마
-- 모든 엔티티를 포함한 통합 마이그레이션 파일

-- =============================================================================
-- 1. 사용자 관련 테이블
-- =============================================================================

-- 사용자 테이블
CREATE TABLE users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(20) NOT NULL,
    password VARCHAR(250),
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    provider VARCHAR(100),
    provider_id VARCHAR(100)
);

-- 사용자 프로필 테이블 (마이페이지 전용)
CREATE TABLE user_profiles (
    profile_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    job_position VARCHAR(100),
    career_level VARCHAR(50),
    profile_image_url VARCHAR(255),
    self_introduction VARCHAR(1000),

    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 🔥 사용자 랭킹 테이블 (누락되었던 테이블!)
CREATE TABLE user_rankings (
    ranking_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    average_score NUMERIC(5,2) NOT NULL DEFAULT 0.0,
    total_interviews INTEGER NOT NULL DEFAULT 0,
    ranking_score NUMERIC(7,2) NOT NULL DEFAULT 0.0,
    current_rank INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT NOW(),

    CONSTRAINT fk_user_rankings_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- =============================================================================
-- 2. 면접 관련 테이블
-- =============================================================================

-- 면접 세션 테이블
CREATE TABLE interviews (
    interview_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    interview_type VARCHAR(20) NOT NULL,
    job_position VARCHAR(50) NOT NULL,
    career_level VARCHAR(50) NOT NULL,
    question_count INTEGER NOT NULL DEFAULT 0,
    duration_minutes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP,

    CONSTRAINT fk_interviews_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT interviews_interview_type_check CHECK (interview_type IN ('TECHNICAL', 'PRACTICAL', 'BEHAVIORAL', 'COMPREHENSIVE'))
);

-- 면접 질문 테이블
CREATE TABLE interview_questions (
    question_id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,

    CONSTRAINT fk_interview_questions_interview FOREIGN KEY (interview_id) REFERENCES interviews(interview_id) ON DELETE CASCADE
);

-- 면접 답변 테이블
CREATE TABLE interview_answers (
    answer_id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL UNIQUE,
    answer_text TEXT NOT NULL,
    submitted_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_interview_answers_question FOREIGN KEY (question_id) REFERENCES interview_questions(question_id) ON DELETE CASCADE
);

-- 면접 결과 테이블
CREATE TABLE interview_results (
    result_id BIGSERIAL PRIMARY KEY,
    interview_id BIGINT NOT NULL UNIQUE,
    total_score INTEGER NOT NULL,
    grade VARCHAR(20) NOT NULL,
    feedback TEXT NOT NULL,
    recommended_resource TEXT,

    CONSTRAINT fk_interview_results_interview FOREIGN KEY (interview_id) REFERENCES interviews(interview_id) ON DELETE CASCADE,
    CONSTRAINT interview_results_grade_check CHECK (grade IN ('A', 'B', 'C', 'D', 'E', 'F')),
    CONSTRAINT interview_results_total_score_check CHECK (total_score >= 0 AND total_score <= 100)
);

-- =============================================================================
-- 3. 커뮤니티 관련 테이블
-- =============================================================================

-- 커뮤니티 게시글 테이블
CREATE TABLE community_posts (
    post_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    category VARCHAR(50),
    grade VARCHAR(20),
    interview_type VARCHAR(20),
    tech_tag VARCHAR(200),
    level VARCHAR(50),
    score INTEGER,
    interview_feedback TEXT,
    interview_result_id BIGINT,
    summary TEXT,
    like_count INTEGER NOT NULL DEFAULT 0,
    scrap_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    writer_name VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_community_posts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT community_posts_grade_check CHECK (grade IS NULL OR grade IN ('A', 'B', 'C', 'D', 'E', 'F')),
    CONSTRAINT community_posts_interview_type_check CHECK (interview_type IS NULL OR interview_type IN ('TECHNICAL', 'PRACTICAL', 'BEHAVIORAL', 'COMPREHENSIVE')),
    CONSTRAINT community_posts_score_check CHECK (score IS NULL OR (score >= 0 AND score <= 100))
);

-- 댓글 테이블
CREATE TABLE comments (
    comment_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    parent_id BIGINT,
    writer_name VARCHAR(50),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(comment_id) ON DELETE CASCADE
);

-- 좋아요 테이블 (복합키)
CREATE TABLE likes (
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_likes_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE
);

-- 스크랩 테이블
CREATE TABLE scraps (
    scrap_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_scraps_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_scraps_post FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    CONSTRAINT uk_scraps_user_post UNIQUE (user_id, post_id)
);

-- =============================================================================
-- 4. 인덱스 생성 (성능 최적화)
-- =============================================================================

-- 사용자 관련 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);

-- 사용자 랭킹 관련 인덱스
CREATE INDEX idx_user_rankings_score ON user_rankings(ranking_score DESC);
CREATE INDEX idx_user_rankings_total_interviews ON user_rankings(total_interviews DESC);
CREATE INDEX idx_user_rankings_composite ON user_rankings(ranking_score DESC, total_interviews DESC, average_score DESC);

-- 면접 관련 인덱스
CREATE INDEX idx_interviews_user_id ON interviews(user_id);
CREATE INDEX idx_interviews_created_at ON interviews(created_at DESC);
CREATE INDEX idx_interviews_type ON interviews(interview_type);
CREATE INDEX idx_interview_questions_interview_id ON interview_questions(interview_id);
CREATE INDEX idx_interview_answers_question_id ON interview_answers(question_id);
CREATE INDEX idx_interview_results_interview_id ON interview_results(interview_id);

-- 커뮤니티 관련 인덱스
CREATE INDEX idx_community_posts_user_id ON community_posts(user_id);
CREATE INDEX idx_community_posts_created_at ON community_posts(created_at DESC);
CREATE INDEX idx_community_posts_category ON community_posts(category);
CREATE INDEX idx_community_posts_grade ON community_posts(grade);
CREATE INDEX idx_community_posts_type ON community_posts(type);
CREATE INDEX idx_community_posts_deleted ON community_posts(deleted);
CREATE INDEX idx_community_posts_interview_type ON community_posts(interview_type);

CREATE INDEX idx_comments_post_id_created_at ON comments(post_id, created_at DESC);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_deleted ON comments(deleted);

CREATE INDEX idx_likes_post_id ON likes(post_id);
CREATE INDEX idx_likes_user_id ON likes(user_id);

CREATE INDEX idx_scraps_post_id ON scraps(post_id);
CREATE INDEX idx_scraps_user_id ON scraps(user_id);
CREATE INDEX idx_scraps_created_at ON scraps(created_at DESC);

-- =============================================================================
-- 5. 테이블 및 컬럼 주석 (문서화)
-- =============================================================================

COMMENT ON TABLE users IS '사용자 정보';
COMMENT ON COLUMN users.provider IS '로그인 제공자 (LOCAL, GOOGLE)';
COMMENT ON COLUMN users.provider_id IS 'OAuth2 제공자별 사용자 ID';

COMMENT ON TABLE user_profiles IS '사용자 프로필 확장 정보 (마이페이지)';
COMMENT ON COLUMN user_profiles.job_position IS '직무 (백엔드, 프론트엔드, AI 등)';
COMMENT ON COLUMN user_profiles.career_level IS '경력 수준 (신입, 주니어, 시니어 등)';
COMMENT ON COLUMN user_profiles.self_introduction IS 'AI 면접 질문 개인화를 위한 자기소개';

COMMENT ON TABLE user_rankings IS '사용자 랭킹 정보';
COMMENT ON COLUMN user_rankings.average_score IS '최근 10회 면접의 평균 점수';
COMMENT ON COLUMN user_rankings.total_interviews IS '총 면접 참여 횟수';
COMMENT ON COLUMN user_rankings.ranking_score IS '계산된 랭킹 점수 (평균점수 + 참여횟수×5)';
COMMENT ON COLUMN user_rankings.current_rank IS '현재 순위';

COMMENT ON TABLE interviews IS '면접 세션 정보';
COMMENT ON COLUMN interviews.interview_type IS '면접 유형 (TECHNICAL, PRACTICAL, BEHAVIORAL, COMPREHENSIVE)';
COMMENT ON COLUMN interviews.question_count IS '질문 개수';
COMMENT ON COLUMN interviews.duration_minutes IS '면접 소요 시간 (분)';

COMMENT ON TABLE interview_questions IS '면접별 질문 목록';
COMMENT ON TABLE interview_answers IS '면접 질문별 답변';
COMMENT ON TABLE interview_results IS '면접 종료 후 결과 (점수, 등급, 피드백)';

COMMENT ON TABLE community_posts IS '커뮤니티 게시글';
COMMENT ON COLUMN community_posts.type IS '게시글 유형 (GENERAL, INTERVIEW_SHARE 등)';
COMMENT ON COLUMN community_posts.deleted IS '소프트 삭제 플래그';
COMMENT ON COLUMN community_posts.interview_result_id IS '면접 결과 공유 시 연결되는 결과 ID';

COMMENT ON TABLE comments IS '게시글 댓글 (대댓글 지원)';
COMMENT ON TABLE likes IS '게시글 좋아요 (사용자별 중복 방지)';
COMMENT ON TABLE scraps IS '게시글 스크랩 (사용자별 중복 방지)';

-- =============================================================================
-- 6. 시퀀스 초기화 (선택사항)
-- =============================================================================

-- 모든 BIGSERIAL 시퀀스를 1000부터 시작하도록 설정 (ID 충돌 방지)
ALTER SEQUENCE users_user_id_seq RESTART WITH 1000;
ALTER SEQUENCE user_profiles_profile_id_seq RESTART WITH 1000;
ALTER SEQUENCE user_rankings_ranking_id_seq RESTART WITH 1000;
ALTER SEQUENCE interviews_interview_id_seq RESTART WITH 1000;
ALTER SEQUENCE interview_questions_question_id_seq RESTART WITH 1000;
ALTER SEQUENCE interview_answers_answer_id_seq RESTART WITH 1000;
ALTER SEQUENCE interview_results_result_id_seq RESTART WITH 1000;
ALTER SEQUENCE community_posts_post_id_seq RESTART WITH 1000;
ALTER SEQUENCE comments_comment_id_seq RESTART WITH 1000;
ALTER SEQUENCE scraps_scrap_id_seq RESTART WITH 1000;