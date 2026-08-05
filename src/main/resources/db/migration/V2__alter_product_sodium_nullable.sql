-- V2__alter_product_sodium_nullable.sql
--
-- 배경: db_ready_data.csv 8,361행 중 240행(주로 콜라겐 드링크 등 건강기능식품, category_id=1/2)에서
-- sodium 값이 원본(식약처 API)에 아예 표시되어 있지 않음이 확인됨.
-- "표시 없음(값을 모름)"과 "실제 나트륨 0mg"은 서로 다른 의미인데, NOT NULL 제약 하에서는
-- 이를 구분할 방법이 없어 0으로 채울 경우 데이터 정합성이 깨짐.
-- 따라서 sodium을 nullable로 변경하여 "미표시" 상태를 NULL로 명시적으로 남긴다.
--
-- V1은 이미 Supabase에 적용된 상태이므로 수정하지 않고 새 버전으로 추가.

ALTER TABLE product ALTER COLUMN sodium DROP NOT NULL;