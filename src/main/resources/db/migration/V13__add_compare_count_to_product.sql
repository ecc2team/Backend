-- product 테이블에 비교함 담긴 횟수를 저장할 compare_count 컬럼 추가
ALTER TABLE product
    ADD COLUMN compare_count INT NOT NULL DEFAULT 0;
