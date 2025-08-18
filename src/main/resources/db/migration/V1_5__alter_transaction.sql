-- Usunięcie NOT NULL z kolumn from_account_id i to_account_id
ALTER TABLE transaction
    ALTER COLUMN from_account_id DROP NOT NULL,
    ALTER COLUMN to_account_id DROP NOT NULL;

-- Dodanie nowej kolumny occurred_at
ALTER TABLE transaction
    ADD COLUMN occurred_at timestamp;

-- Skopiowanie danych ze starego executed_at / created_at do occurred_at
UPDATE transaction
SET occurred_at = COALESCE(executed_at, created_at);

-- Ustawienie kolumny occurred_at jako NOT NULL
ALTER TABLE transaction
    ALTER COLUMN occurred_at SET NOT NULL;

-- Usunięcie starych kolumn created_at i executed_at
ALTER TABLE transaction
    DROP COLUMN created_at,
    DROP COLUMN executed_at;

-- Zmiana typu kolumny currency z char(3) na varchar(3)
ALTER TABLE transaction
    ALTER COLUMN currency TYPE varchar(3) USING trim(currency);