drop table if exists agnos_logs;
CREATE TABLE agnos_logs(
    time VARCHAR(50),
    user_name VARCHAR(50),
    action_type VARCHAR(50),
    report VARCHAR(50)
);



COPY agnos_logs(time, user_name, action_type, report)
FROM '/media/input.csv'
DELIMITER ','
CSV HEADER;


drop table if exists agnos_logs_cleared;
create table agnos_logs_cleared as
(select
    TO_DATE(time, 'YYYY-MM-DD') date,
    user_name,
    action_type,
    report
from agnos_logs);

drop table if exists final;
create table final as
select
    (EXTRACT ('Year' FROM date))::varchar(4) as year,
    LPAD(EXTRACT ('Month' FROM date)::varchar(2), 2, '0')::varchar(2) as month,
    LPAD(EXTRACT ('Day' FROM date)::varchar(2), 2, '0')::varchar(2) as day,
    user_name,
    action_type,
    report,
    count(1) as number
from agnos_logs_cleared
    group by date, user_name, action_type, report;
