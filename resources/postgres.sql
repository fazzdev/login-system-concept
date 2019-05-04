-- Adminer 4.7.1 PostgreSQL dump

CREATE TABLE "public"."authuser" (
    "username" character varying(255) NOT NULL,
    "password" character varying(255) NOT NULL,
    CONSTRAINT "authuser_pkey" PRIMARY KEY ("username")
) WITH (oids = false);

INSERT INTO "authuser" ("username", "password") VALUES
('John',    'a8cfcd74832004951b4408cdb0a5dbcd8c7e52d43f7fe244bf720582e05241da'),
('Mark',    'd7cda0ca2c8586e512c425368fcb2bba62e81475bfceb4284f4906de8ec242bc'),
('Bill',    'e51783b4d7688ffba51a35d8c9f04041606c0d6fb00bb306fba0f2dcb7e1f890'),
('Peter',   'ea72c79594296e45b8c2a296644d988581f58cfac6601d122ed0a8bd7c02e8bf');

CREATE TABLE "public"."mate" (
    "username" character varying(255) NOT NULL,
    "mate" character varying(255) NOT NULL,
    CONSTRAINT "mate_mate_fkey" FOREIGN KEY (mate) REFERENCES authuser(username) NOT DEFERRABLE,
    CONSTRAINT "mate_username_fkey" FOREIGN KEY (username) REFERENCES authuser(username) NOT DEFERRABLE
) WITH (oids = false);


CREATE SEQUENCE session_sessionid_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1;

CREATE TABLE "public"."session" (
    "sessionid" integer DEFAULT nextval('session_sessionid_seq') NOT NULL,
    "token" character varying(36) NOT NULL,
    "issueddate" timestamptz NOT NULL,
    "expireddate" timestamptz NOT NULL,
    "username" character varying(255) NOT NULL,
    CONSTRAINT "session_sessionid" PRIMARY KEY ("sessionid"),
    CONSTRAINT "session_token" UNIQUE ("token"),
    CONSTRAINT "session_username_fkey" FOREIGN KEY (username) REFERENCES authuser(username) NOT DEFERRABLE
) WITH (oids = false);

INSERT INTO "session" ("sessionid", "token", "issueddate", "expireddate", "username") VALUES
(3, '62e556db-d128-4c2d-8cae-14c383771ef8', '2019-05-04 23:29:44.042+00',   '2019-05-05 23:29:44.042+00',   'John');


CREATE SEQUENCE secret_secretid_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1;

CREATE TABLE "public"."secret" (
    "secretid" integer DEFAULT nextval('secret_secretid_seq') NOT NULL,
    "content" text NOT NULL,
    "owner" character varying(255) NOT NULL,
    CONSTRAINT "secret_secretid" PRIMARY KEY ("secretid"),
    CONSTRAINT "Sscret_owner_fkey" FOREIGN KEY (owner) REFERENCES authuser(username) NOT DEFERRABLE
) WITH (oids = false);


CREATE SEQUENCE permission_permissionid_seq INCREMENT 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1;

CREATE TABLE "public"."permission" (
    "permissionid" integer DEFAULT nextval('permission_permissionid_seq') NOT NULL,
    "secretid" integer NOT NULL,
    "username" character varying(255) NOT NULL,
    CONSTRAINT "permission_permissionid" PRIMARY KEY ("permissionid"),
    CONSTRAINT "permission_Username_fkey" FOREIGN KEY (username) REFERENCES authuser(username) NOT DEFERRABLE,
    CONSTRAINT "permission_secretid_fkey" FOREIGN KEY (secretid) REFERENCES secret(secretid) NOT DEFERRABLE
) WITH (oids = false);

-- 2019-05-04 23:38:24.310654+00
