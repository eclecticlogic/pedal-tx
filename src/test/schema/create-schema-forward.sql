create schema forward; set search_path to forward;


CREATE TABLE "exotic_types"(
 "login" Varchar NOT NULL,
 "countries" Bit(7) NOT NULL,
 "authorizations" Character varying(20)[] NOT NULL,
 "scores" Bigint[] NOT NULL,
 "status" Character(2) NOT NULL
)
WITH (OIDS=FALSE)
;

CREATE TABLE "widget"(
 "id" Serial NOT NULL,
 "name" Varchar NOT NULL,
 "login_type" Varchar NOT NULL
)
WITH (OIDS=FALSE)
;


CREATE TABLE "simple_type"(
 "id" Serial NOT NULL,
 "amount" integer NOT NULL
)
WITH (OIDS=FALSE)
;


ALTER TABLE "exotic_types" ADD CONSTRAINT "Key1" PRIMARY KEY ("login")
;
ALTER TABLE "widget" ADD CONSTRAINT "Key2" PRIMARY KEY ("id")
;
ALTER TABLE "simple_type" ADD CONSTRAINT "Key3" PRIMARY KEY ("id")
;

ALTER TABLE "widget" ADD CONSTRAINT "Relationship1" FOREIGN KEY ("login_type") REFERENCES "exotic_types" ("login") ON DELETE NO ACTION ON UPDATE NO ACTION
;


