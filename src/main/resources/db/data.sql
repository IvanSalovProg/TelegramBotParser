DROP TABLE IF EXISTS VACANCY;
DROP SEQUENCE IF EXISTS VACANCY_ID_SEQ;

create table VACANCY
(
    ID bigserial primary key,
    NAME varchar(1024) not null,
    COMPANY varchar(1024) not null,
    TYPE varchar(1024) not null,
    LOCATION varchar(1024) not null,
    SCHEDULE varchar(1024),
    GRADE varchar(1024),
    URL varchar(1024) not null
);