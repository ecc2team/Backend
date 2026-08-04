
-- ------------------------------------------------------------
-- 1. 유저 및 인증 도메인
-- ------------------------------------------------------------
CREATE TABLE users (
                       id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,    -- 회원 고유 ID
                       email       VARCHAR(100) NOT NULL,                              -- 로그인 이메일 (중복 불가)
                       password    VARCHAR(100),                                       -- 비밀번호 (소셜 로그인은 Null)
                       provider    VARCHAR(20),                                        -- 가입 채널 (LOCAL, KAKAO, NAVER 등)
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,     -- 가입 일시
                       deleted_at  TIMESTAMPTZ,                                        -- 탈퇴 일시 (소프트 딜리트용)
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_allergy (
                              id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- 알레르기 설정 고유 ID
                              user_id       BIGINT NOT NULL,                                  -- 회원 ID
                              ingredient_id BIGINT NOT NULL,                                  -- 알레르기 성분 ID
                              created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- 설정 일시  (※ 쉼표 누락 수정)
                              CONSTRAINT fk_user_allergy_user FOREIGN KEY (user_id) REFERENCES users(id),
    -- ingredient FK는 ingredient 테이블 생성 이후 추가
                              CONSTRAINT uk_user_allergy_user_ingredient UNIQUE (user_id, ingredient_id)
);

CREATE TABLE user_preferred_ingredient (
                                           id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- 선호 성분 설정 고유 ID
                                           user_id       BIGINT NOT NULL,                                  -- 회원 ID
                                           ingredient_id BIGINT NOT NULL,                                  -- 선호 성분 ID
                                           created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,   -- 설정 일시  (※ 쉼표 누락 수정)
                                           CONSTRAINT fk_user_pref_ingredient_user FOREIGN KEY (user_id) REFERENCES users(id),
    -- ingredient FK는 ingredient 테이블 생성 이후 추가
                                           CONSTRAINT uk_user_pref_ingredient_user_ingredient UNIQUE (user_id, ingredient_id)
);

CREATE TABLE email_verification (
                                    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,    -- 인증 내역 고유 ID
                                    email       VARCHAR(100) NOT NULL,                              -- 인증 대상 이메일
                                    code        VARCHAR(20) NOT NULL,                               -- 발송된 인증번호
                                    expired_at  TIMESTAMPTZ NOT NULL,                               -- 만료 일시
                                    is_verified BOOLEAN NOT NULL DEFAULT FALSE,                     -- 인증 성공 여부
                                    CONSTRAINT uk_email_verification_email UNIQUE (email)
);

-- ------------------------------------------------------------
-- 2. 상품 및 성분 도메인
-- ------------------------------------------------------------

CREATE TABLE category (
                          id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,              -- 카테고리 고유 ID
                          name VARCHAR(50) NOT NULL                                          -- 카테고리명 (음료류 등)
);

-- 카테고리 시드 데이터 (product 데이터 매핑 시 이 id 값 기준으로 사용)
INSERT INTO category (name) VALUES
                                ('음료류'),
                                ('과자류·빵류 또는 떡류'),
                                ('코코아가공품류 또는 초콜릿류');

CREATE TABLE product (
                         id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- 상품 고유 ID
                         category_id      BIGINT NOT NULL,                                  -- 소속 카테고리 ID
                         external_code    VARCHAR(100),                                     -- 식약처 품목제조보고번호 등 외부 식별 코드
                         name             VARCHAR(100) NOT NULL,                            -- 상품명
                         image_url        VARCHAR(255),                                     -- 상품 썸네일 이미지 주소
                         grade            SMALLINT NOT NULL,                                -- 제로픽 등급 (1, 2, 3)
                         warning_additive BOOLEAN NOT NULL DEFAULT FALSE,                   -- 유해 첨가물 포함 여부
                         calories         INT NOT NULL,                                     -- 총 칼로리 함량 (kcal)
                         sugar            DECIMAL(5,2) NOT NULL,                            -- 총 당류 함량 (g)
                         sodium           DECIMAL(7,2) NOT NULL,                            -- 총 나트륨 함량 (mg)
                         deleted_at       TIMESTAMPTZ,                                      -- 소프트 딜리트용
                         CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
                         CONSTRAINT uk_product_external_code UNIQUE (external_code)
);

CREATE TABLE ingredient (
                            id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 성분 고유 ID
                            code               VARCHAR(50) NOT NULL,                            -- 성분 식별 코드 (예: MALTITOL)
                            name               VARCHAR(50) NOT NULL,                            -- 성분명 (예: 말티톨)
                            ingredient_type    VARCHAR(20) NOT NULL,                            -- 성분 분류 (SWEETENER, ADDITIVE)
                            risk_level         VARCHAR(20) NOT NULL,                            -- 위험도 (PREMIUM, GENERAL, WARNING)
                            summary            VARCHAR(100),                                    -- 뱃지 요약 텍스트
                            description        TEXT,                                            -- 팝업용 상세 설명 텍스트
                            CONSTRAINT uk_ingredient_code UNIQUE (code)
);

-- 앞서 미뤄둔 FK를 여기서 추가
ALTER TABLE user_allergy
    ADD CONSTRAINT fk_user_allergy_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id);

ALTER TABLE user_preferred_ingredient
    ADD CONSTRAINT fk_user_pref_ingredient_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id);

CREATE TABLE product_ingredient (
                                    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- 배합 정보 고유 ID
                                    product_id     BIGINT NOT NULL,                                 -- product 테이블의 ID
                                    ingredient_id  BIGINT NOT NULL,                                 -- ingredient 테이블의 ID
                                    sequence       INT,                                             -- 원재료 표기 순서
                                    CONSTRAINT fk_product_ingredient_product FOREIGN KEY (product_id) REFERENCES product(id),
                                    CONSTRAINT fk_product_ingredient_ingredient FOREIGN KEY (ingredient_id) REFERENCES ingredient(id),
                                    CONSTRAINT uk_product_ingredient UNIQUE (product_id, ingredient_id)
);

-- ------------------------------------------------------------
-- 3. 유저 활동 도메인
-- ------------------------------------------------------------

CREATE TABLE intake_record (
                               id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               user_id     BIGINT NOT NULL,
                               product_id  BIGINT NOT NULL,
                               intake_date DATE NOT NULL,
                               created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_intake_record_user FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT fk_intake_record_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE recent_view (
                             id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,    -- 조회 기록 고유 ID
                             user_id    BIGINT NOT NULL,                                    -- 회원 ID
                             product_id BIGINT NOT NULL,                                    -- 상품 ID
                             viewed_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,     -- 조회 일시
                             CONSTRAINT fk_recent_view_user FOREIGN KEY (user_id) REFERENCES users(id),
                             CONSTRAINT fk_recent_view_product FOREIGN KEY (product_id) REFERENCES product(id),
                             CONSTRAINT uk_recent_view_user_product UNIQUE (user_id, product_id)
);

CREATE TABLE comparison_box (
                                id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                user_id    BIGINT NOT NULL,                                    -- 회원 ID
                                product_id BIGINT NOT NULL,                                    -- 상품 ID
                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_comparison_box_user FOREIGN KEY (user_id) REFERENCES users(id),
                                CONSTRAINT fk_comparison_box_product FOREIGN KEY (product_id) REFERENCES product(id),
                                CONSTRAINT uk_comparison_box_user_product UNIQUE (user_id, product_id)
);

-- ------------------------------------------------------------
-- 4. 인덱스
-- ------------------------------------------------------------

-- 카테고리별 상품 목록 조회
CREATE INDEX idx_product_category_id ON product(category_id);

-- 특정 상품을 섭취한 기록 조회
CREATE INDEX idx_intake_product_id ON intake_record(product_id);

-- 유저별 날짜 범위 섭취 기록 조회 (동등조건 user_id 먼저, 범위조건 intake_date 뒤)
CREATE INDEX idx_intake_user_date ON intake_record(user_id, intake_date);

-- 특정 상품의 최근 조회 이력
CREATE INDEX idx_recent_product_id ON recent_view(product_id);

-- 유저별 최근 본 상품 정렬 조회
CREATE INDEX idx_recent_user_viewed ON recent_view(user_id, viewed_at);

-- 원료 성분 검색용 인덱스 (알레르기/감미료 필터링)
CREATE INDEX idx_product_ingredient_ingredient_id ON product_ingredient(ingredient_id);