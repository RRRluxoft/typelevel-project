CREATE TABLE users (
   email text NOT NULL,
   hashedPassword text NOT NULL,
   firstName text,
   lastName text,
   company text,
   role text NOT NULL
);

ALTER TABLE users
ADD CONSTRAINT pk_users PRIMARY KEY (email);

INSERT INTO users (
   email,
   hashedPassword,
   firstName,
   lastName,
   company,
   role
) VALUES (
  'tom@smartland.com',
  'rockthejvm',
  'Tom',
  'Cat',
  'Smart.Land',
  'ADMIN'
);
INSERT INTO users (
   email,
   hashedPassword,
   firstName,
   lastName,
   company,
   role
) VALUES (
  'rick@smartland.com',
  'rickthere',
  'Rick',
  'Sanchos',
  'Smart.Land',
  'RECRUITER'
);