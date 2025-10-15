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
dueDate DATE not null,
returnDate DATE,
status enum('BORROWED','RETURNED','OVERDUE') not null default 'BORROWED',
fineAmount double default 0,
createdAt DATE not null,
foreign key (memberId) references members(id) on delete cascade,
foreign key (isbn) references books(isbn) on delete cascade
);


insert into users(name, username, password, role, isActive, createdAt) values
('Admin User', 'admin', 'admin123', 'ADMIN', true, CURDATE()),
('Assistant User', 'assistant', 'assist123', 'ASSISTANT', true, CURDATE());

-- Pasame insersiones de prueba para libros y miembros
insert into books(isbn, title, author, category, quantity, available, price, isActive, createdAt) values
('978-3-16-148410-0', 'The Great Gatsby', 'F. Scott Fitzgerald', 'FICTION', 10, 10, 15.99, true, CURDATE()),
('978-0-14-044913-6', 'Crime and Punishment', 'Fyodor Dostoevsky', 'FICTION', 5, 5, 12.99, true, CURDATE()),
('978-0-06-112008-4', 'To Kill a Mockingbird', 'Harper Lee', 'FICTION', 8, 8, 14.99, true, CURDATE()),
('978-0-307-74176-9', 'The Immortal Life of Henrietta Lacks', 'Rebecca Skloot', 'NON_FICTION', 7, 7, 13.99, true, CURDATE()),
('978-1-4000-3341-6', 'A Brief History of Time', 'Stephen Hawking', 'SCIENCE', 6, 6, 18.99, true, CURDATE());

insert into members(name, email, phone, isActive, createdAt) values
('John Doe', 'john.doe@example.com', '123-456-7890', true, CURDATE()),
('Jane Smith', 'jane.smith@example.com', '098-765-4321', true, CURDATE()),
('Alice Johnson', 'alice.johnson@example.com', '555-123-4567', true, CURDATE()),
('Bob Brown', 'bob.brown@example.com', '555-987-6543', true, CURDATE()),
('Charlie Davis', 'charlie.davis@example.com', '555-555-5555', true, CURDATE());