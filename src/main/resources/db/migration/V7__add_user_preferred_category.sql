ALTER TABLE category ADD COLUMN code VARCHAR(30);

UPDATE category SET code = 'DRINK' WHERE name = '음료류';
UPDATE category SET code = 'SNACK' WHERE name = '과자류·빵류 또는 떡류';
UPDATE category SET code = 'CHOCOLATE' WHERE name = '코코아가공품류 또는 초콜릿류';

ALTER TABLE category ALTER COLUMN code SET NOT NULL;
ALTER TABLE category ADD CONSTRAINT uk_category_code UNIQUE (code);

CREATE TABLE user_preferred_category (
                                         id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                         user_id     BIGINT NOT NULL,
                                         category_id BIGINT NOT NULL,
                                         created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         CONSTRAINT fk_user_pref_category_user FOREIGN KEY (user_id) REFERENCES users(id),
                                         CONSTRAINT fk_user_pref_category_category FOREIGN KEY (category_id) REFERENCES category(id),
                                         CONSTRAINT uk_user_pref_category_user_category UNIQUE (user_id, category_id)
);

ALTER TABLE users ADD COLUMN nickname VARCHAR(30) NOT NULL DEFAULT '';