CREATE TABLE IDN_BASE_TABLE (
            PRODUCT_NAME VARCHAR2 (20),
            PRIMARY KEY (PRODUCT_NAME))
/
INSERT INTO IDN_BASE_TABLE values ('WSO2 Identity Server')
/
CREATE TABLE IDN_OAUTH_CONSUMER_APPS (
            CONSUMER_KEY VARCHAR2 (512),
            CONSUMER_SECRET VARCHAR2 (512),
            USERNAME VARCHAR2 (255),
            TENANT_ID INTEGER DEFAULT 0,
            APP_NAME VARCHAR2 (255),
            OAUTH_VERSION VARCHAR2 (128),
            CALLBACK_URL VARCHAR2 (1024),
            LOGIN_PAGE_URL VARCHAR (1024),
            ERROR_PAGE_URL VARCHAR (1024),
            CONSENT_PAGE_URL VARCHAR (1024),
            GRANT_TYPES VARCHAR (1024),
            PRIMARY KEY (CONSUMER_KEY))
/
CREATE TABLE IDN_OAUTH1A_REQUEST_TOKEN (
            REQUEST_TOKEN VARCHAR2 (512),
            REQUEST_TOKEN_SECRET VARCHAR2 (512),
            CONSUMER_KEY VARCHAR2 (512),
            CALLBACK_URL VARCHAR2 (1024),
            SCOPE VARCHAR2(2048),
            AUTHORIZED VARCHAR2 (128),
            OAUTH_VERIFIER VARCHAR2 (512),
            AUTHZ_USER VARCHAR2 (512),
            PRIMARY KEY (REQUEST_TOKEN),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY) ON DELETE CASCADE)
/
CREATE TABLE IDN_OAUTH1A_ACCESS_TOKEN (
            ACCESS_TOKEN VARCHAR2 (512),
            ACCESS_TOKEN_SECRET VARCHAR2 (512),
            CONSUMER_KEY VARCHAR2 (512),
            SCOPE VARCHAR2(2048),
            AUTHZ_USER VARCHAR2 (512),
            PRIMARY KEY (ACCESS_TOKEN),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY) ON DELETE CASCADE)
/
CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE (
            AUTHORIZATION_CODE VARCHAR2 (512),
            CONSUMER_KEY VARCHAR2 (512),
            SCOPE VARCHAR2(2048),
            AUTHZ_USER VARCHAR2 (512),
            TIME_CREATED TIMESTAMP,
            VALIDITY_PERIOD NUMBER(19),
            PRIMARY KEY (AUTHORIZATION_CODE),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY) ON DELETE CASCADE)
/
CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN (
			ACCESS_TOKEN VARCHAR2 (255),
			REFRESH_TOKEN VARCHAR2 (255),
			CONSUMER_KEY VARCHAR2 (255),
			AUTHZ_USER VARCHAR2 (255),
			USER_TYPE VARCHAR (25),
			TIME_CREATED TIMESTAMP,
			VALIDITY_PERIOD NUMBER(19),
			TOKEN_SCOPE VARCHAR2 (25),
			TOKEN_STATE VARCHAR2 (25) DEFAULT 'ACTIVE',
			TOKEN_STATE_ID VARCHAR (256) DEFAULT 'NONE',
			PRIMARY KEY (ACCESS_TOKEN),
            FOREIGN KEY (CONSUMER_KEY) REFERENCES IDN_OAUTH_CONSUMER_APPS(CONSUMER_KEY) ON DELETE CASCADE,
            CONSTRAINT CON_APP_KEY UNIQUE (CONSUMER_KEY, AUTHZ_USER,USER_TYPE,TOKEN_SCOPE,TOKEN_STATE,TOKEN_STATE_ID))
/
CREATE TABLE UM_USER_ATTRIBUTES (
                    ID INTEGER,
                    ATTR_NAME VARCHAR2(255) NOT NULL,
                    ATTR_VALUE VARCHAR2(255),
                    USER_ID INTEGER,
                    --FOREIGN KEY (USER_ID) REFERENCES UM_USER(UM_ID) ON DELETE CASCADE,
                    PRIMARY KEY (ID))
/
CREATE TABLE IDN_SCIM_GROUP (
			ID INTEGER,
			TENANT_ID INTEGER NOT NULL,
			ROLE_NAME VARCHAR2(255) NOT NULL,
            ATTR_NAME VARCHAR2(1024) NOT NULL,
			ATTR_VALUE VARCHAR2(1024),
            PRIMARY KEY (ID))
/
CREATE SEQUENCE IDN_SCIM_GROUP_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/

CREATE OR REPLACE TRIGGER IDN_SCIM_GROUP_TRIGGER
		            BEFORE INSERT
                    ON IDN_SCIM_GROUP
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                     SELECT IDN_SCIM_GROUP_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE IDN_SCIM_PROVIDER (
            CONSUMER_ID VARCHAR(255) NOT NULL,
            PROVIDER_ID VARCHAR(255) NOT NULL,
            USER_NAME VARCHAR(255) NOT NULL,
            USER_PASSWORD VARCHAR(255) NOT NULL,
            USER_URL VARCHAR(1024) NOT NULL,
			GROUP_URL VARCHAR(1024),
			BULK_URL VARCHAR(1024),
            PRIMARY KEY (CONSUMER_ID,PROVIDER_ID))
/
CREATE TABLE IDN_OPENID_REMEMBER_ME (
            USER_NAME VARCHAR(255) NOT NULL,
            TENANT_ID INTEGER DEFAULT 0,
            COOKIE_VALUE VARCHAR(1024),
            CREATED_TIME TIMESTAMP,
            PRIMARY KEY (USER_NAME, TENANT_ID))
/
CREATE TABLE IDN_OPENID_USER_RPS (
			USER_NAME VARCHAR(255) NOT NULL,
			TENANT_ID INTEGER DEFAULT 0,
			RP_URL VARCHAR(255) NOT NULL,
			TRUSTED_ALWAYS VARCHAR(128) DEFAULT 'FALSE',
			LAST_VISIT DATE NOT NULL,
			VISIT_COUNT INTEGER DEFAULT 0,
			DEFAULT_PROFILE_NAME VARCHAR(255) DEFAULT 'DEFAULT',
			PRIMARY KEY (USER_NAME, TENANT_ID, RP_URL))
/
CREATE TABLE IDN_OPENID_ASSOCIATIONS (
			HANDLE VARCHAR(255) NOT NULL,
			ASSOC_TYPE VARCHAR(255) NOT NULL,
			EXPIRE_IN TIMESTAMP NOT NULL,
			MAC_KEY VARCHAR(255) NOT NULL,
			ASSOC_STORE VARCHAR(128) DEFAULT 'SHARED',
			PRIMARY KEY (HANDLE))
/
CREATE TABLE IDN_STS_STORE (
                        ID INTEGER,
                        TOKEN_ID VARCHAR(255) NOT NULL,
                        TOKEN_CONTENT BLOB NOT NULL,
                        CREATE_DATE TIMESTAMP NOT NULL,
                        EXPIRE_DATE TIMESTAMP NOT NULL,
                        STATE INTEGER DEFAULT 0,
                        PRIMARY KEY (ID))
/
CREATE SEQUENCE IDN_STS_STORE_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/

CREATE OR REPLACE TRIGGER IDN_STS_STORE_TRIGGER
		            BEFORE INSERT
                    ON IDN_STS_STORE
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                    BEGIN
                     SELECT IDN_STS_STORE_SEQUENCE.nextval INTO :NEW.ID FROM dual;
                    END;
/
CREATE TABLE IDN_IDENTITY_USER_DATA (
                        TENANT_ID INTEGER DEFAULT -1234,
                        USER_NAME VARCHAR(255) NOT NULL,
                        DATA_KEY VARCHAR(255) NOT NULL,
                        DATA_VALUE VARCHAR(255) NOT NULL,
                        PRIMARY KEY (TENANT_ID, USER_NAME, DATA_KEY))
/
CREATE TABLE IDN_IDENTITY_META_DATA (
                        USER_NAME VARCHAR(255) NOT NULL,
                        TENANT_ID INTEGER DEFAULT -1234,
                        METADATA_TYPE VARCHAR(255) NOT NULL,
                        METADATA VARCHAR(255) NOT NULL,
                        VALID VARCHAR(255) NOT NULL,
            PRIMARY KEY (TENANT_ID, USER_NAME, METADATA_TYPE,METADATA))
/
CREATE TABLE UM_TENANT_IDP (
			UM_ID INTEGER NOT NULL,
			UM_TENANT_ID INTEGER NOT NULL,
			UM_TENANT_IDP_NAME VARCHAR(512),
			UM_TENANT_IDP_ISSUER VARCHAR(512),
            		UM_TENANT_IDP_URL VARCHAR(2048),
			UM_TENANT_IDP_THUMBPRINT VARCHAR(2048),
			UM_TENANT_IDP_PRIMARY CHAR(1) NOT NULL,
			PRIMARY KEY (UM_ID),
			CONSTRAINT CON_IDP_KEY UNIQUE (UM_TENANT_ID, UM_TENANT_IDP_NAME))
/

CREATE SEQUENCE IDP_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/
CREATE OR REPLACE TRIGGER IDP_TRIGGER
                    BEFORE INSERT
                    ON UM_TENANT_IDP
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                     BEGIN
                       SELECT IDP_SEQUENCE.nextval INTO :NEW.UM_ID FROM dual;
 			   END;
/
CREATE TABLE UM_TENANT_IDP_ROLES (
			UM_ID INTEGER NOT NULL,
			UM_TENANT_IDP_ID INTEGER NOT NULL,
			UM_TENANT_IDP_ROLE VARCHAR(512),
			PRIMARY KEY (UM_ID),
			CONSTRAINT CON_ROLES_KEY UNIQUE (UM_TENANT_IDP_ID, UM_TENANT_IDP_ROLE),
			FOREIGN KEY (UM_TENANT_IDP_ID) REFERENCES UM_TENANT_IDP(UM_ID) ON DELETE CASCADE)
/
CREATE SEQUENCE IDP_ROLES_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/
CREATE OR REPLACE TRIGGER IDP_ROLES_TRIGGER
                    BEFORE INSERT
                    ON UM_TENANT_IDP_ROLES
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                     BEGIN
                       SELECT IDP_ROLES_SEQUENCE.nextval INTO :NEW.UM_ID FROM dual;
 			   END;
/
CREATE TABLE UM_TENANT_IDP_ROLE_MAPPINGS (
			UM_ID INTEGER NOT NULL,
			UM_TENANT_IDP_ROLE_ID INTEGER NOT NULL,
			UM_TENANT_ID INTEGER NOT NULL,
			UM_TENANT_ROLE VARCHAR(512),	
			PRIMARY KEY (UM_ID),
			CONSTRAINT CON_ROLE_MAPPINGS_KEY UNIQUE (UM_TENANT_IDP_ROLE_ID, UM_TENANT_ID, UM_TENANT_ROLE),
			FOREIGN KEY (UM_TENANT_IDP_ROLE_ID) REFERENCES UM_TENANT_IDP_ROLES(UM_ID) ON DELETE CASCADE)
/

CREATE SEQUENCE IDP_ROLE_MAPPINGS_SEQUENCE START WITH 1 INCREMENT BY 1 NOCACHE
/
CREATE OR REPLACE TRIGGER IDP_ROLE_MAPPINGS_TRIGGER
                    BEFORE INSERT
                    ON UM_TENANT_IDP_ROLE_MAPPINGS
                    REFERENCING NEW AS NEW
                    FOR EACH ROW
                     BEGIN
                       SELECT IDP_ROLE_MAPPINGS_SEQUENCE.nextval INTO :NEW.UM_ID FROM dual;
 			   END;
/
