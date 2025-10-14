create table users(
id int primary key auto_increment,
name VARCHAR(75) not null,
username VARCHAR(75) not null unique,
password varchar(75) not null,
role enum('ADMIN','ASSISTANT')
);

create table books(
isbn varchar(155) primary KEY,
title varchar(255) not null,
author varchar(255) not null,
category enum() not null default 'UKNOWN',
quantity int check(quantity tity >= 0) not null,
available int check(available tity >= 0) not null,
price double check(price >= 0) not null,
isActive boolean not null,
createdAt DATE not null default now()
);

create table members(
id int primary key auto_increment,
name varchar(255) not null,
email varchar(255) not null unique,
phone varchar(15) not null unique,
address varchar(255) not null,
isActive boolean not null,
createdAt DATE not null default now()
);

create table borrow_records(
id int primary key auto_increment,
memberId int not null,
isbn varchar(155) not null,
borrowDate DATE not null default now(),
returnDate DATE,
status enum('BORROWED','RETURNED','OVERDUE') not null default 'BORROWED',
foreign key (memberId) references members(id) on delete cascade,
foreign key (isbn) references books(isbn) on delete cascade
);


insert into users(name, username, password, role) values
('Admin User', 'admin', 'admin123', 'ADMIN'),
('Assistant User', 'assistant', 'assist123', 'ASSISTANT');