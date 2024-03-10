use testowa;

create table temperature_measurement (
    id int not null primary key,
    roomId int not null,
    thermometerId int not null,
    temperature decimal(10, 2)
)
