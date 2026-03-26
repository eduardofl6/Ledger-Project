
CREATE TABLE IF NOT EXISTS transEventStored (
    id         bigserial   primary key,
    accountId  varchar(36) not null,
    payload    jsonb       not null,
    date_occur timestamptz not null DEFAULT now()
);

CREATE TABLE IF NOT EXISTS transOutBox (
    id         bigserial   primary key,
    accountId  varchar(36) not null,
    payload    jsonb       not null,
    published  boolean     not null default false,
    date_occur timestamptz not null default now()
);


CREATE TABLE IF NOT EXISTS transAuditLog(
    id         bigserial   primary key,
    accountId  varchar(36) not null,
    payload    jsonb       not null,
    done_at    timestamptz not null default now()
);

CREATE TABLE IF NOT EXISTS accountsBalance (
    accountId       varchar(36) primary key,
    balance         NUMERIC not null
);

CREATE TABLE IF NOT EXISTS accountsFraud (
    accountId   varchar(36) primary key,
    fraudAlert  boolean     not null default false
);

CREATE TABLE IF NOT EXISTS transFraudLog (
   id          bigserial    primary key,
   accountId   varchar(36)  not null,
   reason      varchar(500) not null,
   payload     jsonb        not null,
   flagged_at  timestamptz  not null default now()
);
