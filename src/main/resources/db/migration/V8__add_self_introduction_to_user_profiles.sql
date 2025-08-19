-- V7__add_self_introduction_to_user_profiles.sql
-- 자기소개 필드를 user_profiles 테이블에 추가

-- self_introduction 컬럼 추가
ALTER TABLE user_profiles
ADD COLUMN self_introduction VARCHAR(1000);

-- 컬럼에 주석 추가 (PostgreSQL 스타일)
COMMENT ON COLUMN user_profiles.self_introduction IS 'AI 면접 질문 개인화를 위한 자기소개 (프론트엔드에서 한글 200자 제한)';

-- 기존 데이터에 대한 기본값은 NULL로 유지 (선택 사항이므로)