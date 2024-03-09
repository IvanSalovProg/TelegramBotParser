DROP TABLE IF EXISTS VACANCY;
DROP SEQUENCE IF EXISTS VACANCY_ID_SEQ;
DROP TABLE IF EXISTS USER_CHAT;
DROP SEQUENCE IF EXISTS USER_CHAT_ID_SEQ;

create table VACANCY
(
    ID bigserial primary key,
    NAME varchar(1024),
    COMPANY varchar(1024),
    LOCATION varchar(1024),
    SCHEDULE varchar(1024),
    GRADE varchar(1024),
    SITE smallint,
    TYPE smallint,
    URL varchar(1024),
    DATE_TIME timestamp
);

create table USER_CHAT
(
    ID bigserial primary key,
    EMAIL varchar(1024),
    CHAT_ID integer,
    TYPE smallint,
    GRADE varchar(1024),
    NAME varchar(1024)
);