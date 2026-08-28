-- 신체 정보 및 활동량 컬럼 추가
ALTER TABLE users ADD COLUMN gender VARCHAR(10);             -- MALE, FEMALE
ALTER TABLE users ADD COLUMN birth_date DATE;                 -- YYYY-MM-DD
ALTER TABLE users ADD COLUMN height DECIMAL(5,1);            -- 예: 175.5 (cm)
ALTER TABLE users ADD COLUMN weight DECIMAL(5,1);            -- 예: 68.0 (kg)
ALTER TABLE users ADD COLUMN activity_level VARCHAR(30);     -- SEDENTARY, LIGHT, MODERATE, VERY_ACTIVE