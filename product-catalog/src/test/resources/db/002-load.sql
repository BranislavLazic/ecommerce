use productcatalog;

-- populate category table
set @cat_home_garden = unhex(replace(uuid(), '-', ''));
set @cat_kitchen     = unhex(replace(uuid(), '-', ''));
set @cat_furniture   = unhex(replace(uuid(), '-', ''));
insert into category (categoryid, categoryname) values (@cat_home_garden, 'home & garden');
insert into category (categoryid, categoryname) values (@cat_kitchen, 'kitchen');
insert into category (categoryid, categoryname) values (@cat_furniture, 'furniture');

-- populate manufacturer table
set @man_black_decker = unhex(replace(uuid(), '-', ''));
set @man_calphalon    = unhex(replace(uuid(), '-', ''));
set @man_kitchen_aid  = unhex(replace(uuid(), '-', ''));
set @man_lazy_boy     = unhex(replace(uuid(), '-', ''));
insert into manufacturer (manufacturerid, manufacturername) values (@man_black_decker, 'black & decker');
insert into manufacturer (manufacturerid, manufacturername) values (@man_calphalon, 'calphalon');
insert into manufacturer (manufacturerid, manufacturername) values (@man_kitchen_aid, 'kitchen aid');
insert into manufacturer (manufacturerid, manufacturername) values (@man_lazy_boy, 'lazy boy');

-- populate product table
insert into product (productid, categoryid, manufacturerid, productcode, displayname, description, price) values (unhex(replace(uuid(), '-', '')), @cat_home_garden, @man_black_decker, 'B56X87', 'outdoor grill', 'for all your bbq needs', 129.99);
insert into product (productid, categoryid, manufacturerid, productcode, displayname, description, price) values (unhex(replace(uuid(), '-', '')), @cat_kitchen, @man_calphalon, 'Y22Q41', 'stainless steel set', 'the best quality for profesional style cooking', 399.00);
insert into product (productid, categoryid, manufacturerid, productcode, displayname, description, price) values (unhex(replace(uuid(), '-', '')), @cat_kitchen, @man_kitchen_aid, 'T43A01', 'can opener', 'opens every time without a hitch', 14.99);
insert into product (productid, categoryid, manufacturerid, productcode, displayname, description, price) values (unhex(replace(uuid(), '-', '')), @cat_furniture, @man_lazy_boy, 'R87P21', 'recliner', 'the best comfort for your netflix binging', 449.99);