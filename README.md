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

```sbt
sbt `runMain com.smartland.jobsboard.Application`

```

or in build.sbt add:

```
Compile / mainClass := Some("com.smartland.jobsboard.Application")
```

and just:

```sbt
sbt run
```

```sbt
sbt ~ compile

```

<br>

Http request by <i>httpie</i> utility:

```bash
http POST localhost:9091/api/jobs/create < src/main/resources/payloads/jobInfo.json
```

Other Endpoints:

1. all jobs:

```bash 
http POST localhost:9091/api/jobs
```

2. find job by UUID:

```bash
http GET localhost:9091/api/jobs/bef585d2-6114-4c98-9ae0-2fb0fd504e97
```

3. create a job:

```bash
http POST localhost:9091/api/jobs/bef585d2-6114-4c98-9ae0-2fb0fd504e97
```

4. update the job:

```bash
http PUT localhost:9091/api/jobs/bef585d2-6114-4c98-9ae0-2fb0fd504e97 { jobInfo }
```

5. delete the job:

```bash
http DELETE localhost:9091/api/jobs/bef585d2-6114-4c98-9ae0-2fb0fd504e97
```

# Before sbt test make sure that Docker run !!!

to
avoid ```ERROR org.testcontainers.dockerclient.DockerClientProviderStrategy -- Could not find a valid Docker environment.```