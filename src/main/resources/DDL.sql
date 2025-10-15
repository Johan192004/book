create table users(
id int primary key auto_increment,
name VARCHAR(75) not null,
username VARCHAR(75) not null unique,
password varchar(75) not null,
role enum('ADMIN','ASSISTANT') not null default 'ASSISTANT',
isActive boolean not null default true,
createdAt DATE not null
);

create table books(
isbn varchar(155) primary KEY,
title varchar(255) not null,
author varchar(255) not null,
category enum('UNKNOWN','FICTION','NON_FICTION','SCIENCE','TECHNOLOGY','HISTORY','OTHERS') not null default 'UNKNOWN'
quantity int check(quantity >= 0) not null,
available int check(available >= 0) not null,
price Double check(price >= 0) not null,
isActive boolean not null,
createdAt DATE not null
);

create table members(
id int primary key auto_increment,
name varchar(255) not null,
email varchar(255) not null unique,
phone varchar(15) not null unique,
isActive boolean not null,
createdAt DATE not null default
);

create table loans(
id int primary key auto_increment,
memberId int not null,
isbn varchar(155) not null,
borrowDate DATE not null,
returnDate DATE not null,
status enum('BORROWED','RETURNED','OVERDUE') not null default 'BORROWED',
foreign key (memberId) references members(id) on delete cascade,
foreign key (isbn) references books(isbn) on delete cascade
);


insert into users(name, username, password, role, isActive, createdAt) values
('Admin User', 'admin', 'admin123', 'ADMIN', true, CURDATE()),
('Assistant User', 'assistant', 'assist123', 'ASSISTANT', true, CURDATE());