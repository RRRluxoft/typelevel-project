## Smart Land walk through Type Level with Cats ##
 
### Dooibe foundations: 
```bash
colima start
```
and then 
```bash
docker-compose up db -d
```
and then
```bash
docker exec -it typelevel-project-db-1 psql -U docker
```
ta-da-ah: `docker=#`
```postgresql
create database demo;
```
```postgresql
\c demo
```
`You are now connected to database "demo" as user "docker".` <br>
`demo=#`

Create a table:
```postgresql
create table students(id serial not null, name character varying not null , primary key (id));
```
```postgresql
insert into students(id, name)
VALUES (1, 'smart land'),
       (2, 'daniel'),
       (3, 'RRR'),
       (4, 'tom cat'),
       (5, 'wild cat'),
       (6, 'beer cat')
;
```