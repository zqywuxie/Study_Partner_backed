# //写一个商品表sql
# 写一个商品表sql，还可以使用积分兑换
create table goods
(
    id     int primary key auto_increment,
    `name`   varchar(20),
    price  double,
    `number` int,
    score  int
);