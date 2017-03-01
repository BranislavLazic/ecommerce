-- create database if not exists productcatalog
-- default character set utf8mb4
-- default collate utf8mb4_unicode_ci;

use productcatalog;

drop table if exists product;
drop table if exists category;
drop table if exists manufacturer;

create table category (
  categoryid binary(16) not null,
  categoryname varchar(50) not null,
  primary key (categoryid)
);

create table manufacturer (
  manufacturerid binary(16) not null,
  manufacturername varchar(50) not null,
  primary key (manufacturerid)
);

create table product (
  productid binary(16) not null,
  categoryid binary(16) not null,
  manufacturerid binary(16) not null,
  productcode varchar(50) not null,
  displayname varchar(50) not null,
  description varchar(500) not null,
  price numeric(15,2) not null default 0.00,
  primary key (productid),
  foreign key fk_cat(categoryid) references category(categoryid),
  foreign key fk_man(manufacturerid) references manufacturer(manufacturerid)
)

