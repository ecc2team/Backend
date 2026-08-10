-- 배경: 기존 UNIQUE(email) 제약은 소프트 삭제 정책과 충돌함.
-- 탈퇴한 유저(deleted_at IS NOT NULL)가 있는 상태에서 같은 이메일로 재가입하면
-- 여전히 테이블에 남아있는 탈퇴 유저 row 때문에 유니크 제약 위반이 발생함.
-- -> 활성 유저(deleted_at IS NULL)에 한해서만 이메일 유니크를 강제하는
--    부분 유니크 인덱스(partial unique index)로 교체.

ALTER TABLE users DROP CONSTRAINT uk_users_email;

CREATE UNIQUE INDEX uk_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;