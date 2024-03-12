CREATE SEQUENCE country_seq START 1 INCREMENT 1;

CREATE TABLE country (
    id INT8 NOT NULL DEFAULT NEXTVAL('country_seq'),
    version INT8 NOT NULL,
    name VARCHAR(20) NOT NULL,
    code VARCHAR(2) NOT NULL,
    PRIMARY KEY (id)
    );

ALTER TABLE country
    ADD CONSTRAINT uc_country_code UNIQUE (code);

INSERT INTO country (name, code, version) VALUES ('Brazil', 'BR', 0);
INSERT INTO country (name, code, version) VALUES ('Spain', 'ES', 0);
INSERT INTO country (name, code, version) VALUES ('Germany', 'DE', 0);
INSERT INTO country (name, code, version) VALUES ('Argentina', 'AR', 0);
INSERT INTO country (name, code, version) VALUES ('England', 'GB', 0);
INSERT INTO country (name, code, version) VALUES ('Italy', 'IT', 0);
