-- src/main/resources/data.sql

-- Spring Session JDBC schema for PostgreSQL
CREATE TABLE SPRING_SESSION (
        PRIMARY_ID CHAR(36) NOT NULL,
        SESSION_ID CHAR(36) NOT NULL,
        CREATION_TIME BIGINT NOT NULL,
        LAST_ACCESS_TIME BIGINT NOT NULL,
        MAX_INACTIVE_INTERVAL INT NOT NULL,
        EXPIRY_TIME BIGINT NOT NULL,
        PRINCIPAL_NAME VARCHAR(100),
        CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
       SESSION_PRIMARY_ID CHAR(36) NOT NULL,
       ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
       ATTRIBUTE_BYTES BYTEA NOT NULL,
       CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
       CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS report_details (
    id BIGSERIAL PRIMARY KEY,
    report_name VARCHAR(255) NOT NULL,
    report_description TEXT,
    incident_name VARCHAR(255),
    incident_description TEXT
    );

CREATE TABLE sumar_2024 (
    id SERIAL PRIMARY KEY,
    dosare INTEGER,
    incompatibilitati INTEGER,
    conflicte_interese INTEGER,
    averi_nejustificate INTEGER,
    valoare_averi NUMERIC(12,2),
    indicii_penale INTEGER,
    persoane_penale INTEGER,
    amenzi INTEGER,
    proceduri_achizitii INTEGER,
    avertismente_integritate INTEGER,
    valoare_integritate NUMERIC(12,2)
);


INSERT INTO report_details (report_name, report_description, incident_name, incident_description)
VALUES
    ('Sales Report',
     'Monthly sales data',
     'Network Outage',
     'Major outage in region A'),

    ('Inventory Report',
     'Current inventory status',
     'Stock Replenishment',
     'Restock completed for warehouse B'),

    ('Customer Report',
     'Customer demographics',
     'Survey',
     'Annual customer feedback survey');

INSERT INTO sumar_2024 (
    dosare,
    incompatibilitati,
    conflicte_interese,
    averi_nejustificate,
    valoare_averi,
    indicii_penale,
    persoane_penale,
    amenzi,
    proceduri_achizitii,
    avertismente_integritate,
    valoare_integritate
) VALUES (
 67,         -- dosare
 25,         -- incompatibilitati
 36,         -- conflicte_interese
 4,          -- averi_nejustificate
 2700000.00, -- valoare_averi
 9,          -- indicii_penale
 9,          -- persoane_penale
 655,        -- amenzi
 19592,      -- proceduri_achizitii
 19,         -- avertismente_integritate
 396000000.00-- valoare_integritate
         );
