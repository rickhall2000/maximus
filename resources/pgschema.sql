-- Table: contact

-- DROP TABLE contact;

CREATE TABLE contact
(
  rowid serial NOT NULL,
  first character varying(50),
  last character varying(50),
  createdon character varying(50),
  email character varying(50),
  postal character varying(80),
  phone character varying(50),
  leadsource character varying(50),
  status integer,
  createdby character varying(50),
  CONSTRAINT pk_rowid PRIMARY KEY (rowid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contact
  OWNER TO notdatomic;

-- Index: idx_status

-- DROP INDEX idx_status;

CREATE INDEX idx_status
  ON contact
  USING btree
  (status);



-- Table: eventx

--  DROP TABLE eventx;

CREATE TABLE eventx
(
  rowid serial NOT NULL,
  contactid integer NOT NULL,
  eventtype character varying(50),
  createdon character varying(50),
  createdby character varying(50),
  agent character varying(50),
  CONSTRAINT pk_eventx PRIMARY KEY (rowid),
  CONSTRAINT fk_eventx_contact FOREIGN KEY (contactid)
      REFERENCES contact (rowid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE eventx
  OWNER TO notdatomic;


-- Table: cardchange

-- DROP TABLE cardchange;

CREATE TABLE cardchange
(
  rowid serial NOT NULL,
  cardid integer NOT NULL,
  fieldname character varying(50) NOT NULL,
  oldval character varying(250),
  newval character varying(250),
  createdon character varying(50),
  createdby character varying(50),
  CONSTRAINT pk_cardchange PRIMARY KEY (rowid),
  CONSTRAINT fk_cardchange_contact FOREIGN KEY (cardid)
      REFERENCES contact (rowid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE cardchange
  OWNER TO notdatomic;
