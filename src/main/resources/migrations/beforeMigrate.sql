CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "hstore";

---
-- TABLE
---

-- tbl_audit_trail definition
CREATE TABLE IF NOT EXISTS tbl_audit_trail (
	id varchar(50) NOT NULL DEFAULT uuid_generate_v4(),
	tname varchar(255) NOT NULL,
	rowid varchar(100) NULL,
	userdb varchar(255) NOT NULL,
	relid oid NOT NULL,
	transid int8 NOT NULL,
	application varchar(255) NULL,
	addr inet NULL,
	port int4 NULL,
	action varchar(2) NOT NULL,
	oldval json NULL,
	newval json NULL,
	transtime timestamptz NOT NULL,
	statetime timestamptz NOT NULL,
	clocktime timestamptz NOT NULL,
	vorder bigserial NOT NULL,
	CONSTRAINT tbl_audit_trail_pkey PRIMARY KEY (id)
);

---
-- INDEXING
---

CREATE INDEX IF NOT EXISTS tbl_audit_trail_relid_idx ON tbl_audit_trail USING btree(relid);
CREATE INDEX IF NOT EXISTS tbl_audit_trail_action_idx ON tbl_audit_trail USING btree(action);
CREATE INDEX IF NOT EXISTS tbl_audit_trail_rowid_idx ON tbl_audit_trail USING btree(rowid);
CREATE INDEX IF NOT EXISTS tbl_audit_trail_transtime_idx ON tbl_audit_trail USING btree(transtime);

---
-- TRIGGER SETUP
---

CREATE OR REPLACE FUNCTION audit_trail_setup() RETURNS trigger LANGUAGE plpgsql AS '
DECLARE
    rowdata tbl_audit_trail;
    oldval hstore;
    newval hstore;
    excludeds text[] = ARRAY[]::text[];
    ignores text[] = ARRAY[''password'', ''credential'']::text;
BEGIN
    IF (TG_LEVEL = ''ROW'') THEN
        rowdata = ROW(
            uuid_generate_v4(),
            TG_TABLE_SCHEMA::text || ''.'' || TG_TABLE_NAME::text,
            NULL,
            session_user::text,
            TG_RELID,
            txid_current(),
            current_setting(''application_name''),
            inet_client_addr(),
            inet_client_port(),
            substring(TG_OP,1,1),
            NULL,
            NULL,
            current_timestamp,
            statement_timestamp(),
            clock_timestamp(),
            nextval(''tbl_audit_trail_vorder_seq''::regclass)
        );

        IF TG_ARGV[1] IS NOT NULL THEN
            excludeds = TG_ARGV[1]::text[];
        END IF;

        IF (TG_OP = ''UPDATE'' OR TG_OP = ''INSERT'') THEN
            newval = hstore(NEW.*);
            IF TG_ARGV[0] IS NOT NULL THEN
                rowdata.rowid = newval->TG_ARGV[0];
            END IF;
            newval = newval - excludeds - ignores;
        END IF;

        IF (TG_OP = ''UPDATE'' OR TG_OP = ''DELETE'') THEN
            oldval = hstore(OLD.*);
            IF TG_ARGV[0] IS NOT NULL THEN
                rowdata.rowid = oldval->TG_ARGV[0];
            END IF;
            oldval = oldval - excludeds - ignores;
        END IF;

        IF (TG_OP = ''UPDATE'') THEN
            IF oldval = newval THEN
                RETURN NULL;
            END IF;
            rowdata.oldval = hstore_to_json(oldval - newval);
            rowdata.newval = hstore_to_json(newval - oldval);
        ELSIF (TG_OP = ''DELETE'') THEN
            rowdata.oldval = hstore_to_json(oldval);
        ELSIF (TG_OP = ''INSERT'') THEN
            rowdata.newval = hstore_to_json(newval);
        ELSE
            RAISE EXCEPTION ''[audit_trail_setup] - Trigger func added as trigger for unhandled case: %'',TG_OP;
            RETURN NULL;
        END IF;
        INSERT INTO tbl_audit_trail VALUES (rowdata.*);
    END IF;
    RETURN NULL;
END;
';

