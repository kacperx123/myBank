alter table account
    alter column currency type varchar(3) using currency::varchar(3);