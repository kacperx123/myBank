create extension if not exists "uuid-ossp";

create table role (
  id   serial primary key,
  name varchar(50) unique not null
);

create table users (
  id         uuid primary key default uuid_generate_v4(),
  email      varchar(255) unique not null,
  password   varchar(255)        not null,
  enabled    boolean             not null default true,
  created_at timestamp           not null default now()
);

create table user_role (
  user_id uuid  not null references users(id) on delete cascade,
  role_id int   not null references role(id)  on delete cascade,
  primary key (user_id, role_id)
);

create table account (
  id              uuid primary key default uuid_generate_v4(),
  account_number  varchar(26) unique not null,
  currency        char(3)            not null,
  balance         numeric(19,4)      not null,
  status          varchar(20)        not null default 'ACTIVE',
  daily_limit     numeric(19,4)      not null default 0,
  locked          boolean            not null default false,
  created_at      timestamp          not null default now(),
  user_id         uuid               not null references users(id)
);

create table transaction (
  id               uuid primary key default uuid_generate_v4(),
  type             varchar(30)       not null,
  amount           numeric(19,4)     not null,
  currency         char(3)           not null,
  from_account_id  uuid              not null references account(id),
  to_account_id    uuid              not null references account(id),
  status           varchar(20)       not null,
  created_at       timestamp         not null default now(),
  executed_at      timestamp
);

create table fx_rate (
  base_currency   char(3) not null,
  target_currency char(3) not null,
  rate_date       date    not null,
  rate            numeric(18,6) not null,
  cached_at       timestamp     not null default now(),
  primary key (base_currency, target_currency, rate_date)
);

create table audit_log (
  id           bigserial primary key,
  user_id      uuid,
  action       varchar(50) not null,
  entity_type  varchar(50) not null,
  entity_id    uuid,
  payload      jsonb,
  created_at   timestamp   not null default now()
);
