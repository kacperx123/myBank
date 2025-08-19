ALTER TABLE fx_rate
    ALTER COLUMN base_currency   TYPE varchar(3) USING trim(base_currency),
    ALTER COLUMN target_currency TYPE varchar(3) USING trim(target_currency);