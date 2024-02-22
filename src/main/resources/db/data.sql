DROP TABLE IF EXISTS VACANCY;
DROP SEQUENCE IF EXISTS VACANCY_ID_SEQ;

create table VACANCY
(
    ID bigserial primary key,
    NAME varchar(1024),
    COMPANY varchar(1024),
    TYPE smallint,
    LOCATION varchar(1024),
    SCHEDULE varchar(1024),
    GRADE varchar(1024),
    URL varchar(1024),
    SITE smallint,
    DATE_TIME timestamp
);