-- 기존 intake_date 컬럼 삭제
ALTER TABLE intake_record DROP COLUMN intake_date;

-- intake_at(섭취 일시) 및 quantity(수량) 컬럼 추가
ALTER TABLE intake_record ADD COLUMN intake_at TIMESTAMPTZ NOT NULL;
ALTER TABLE intake_record ADD COLUMN quantity DECIMAL(3,1) NOT NULL DEFAULT 1.0;