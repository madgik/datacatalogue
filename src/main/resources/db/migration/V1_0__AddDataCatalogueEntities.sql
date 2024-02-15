CREATE TABLE data_model (
                           uuid UUID PRIMARY KEY,
                           code VARCHAR(255) NOT NULL,
                           version VARCHAR(255) NOT NULL,
                           label VARCHAR(255) NOT NULL,
                           longitudinal BOOLEAN NOT NULL,
                           variables TEXT,
                           groups TEXT,
                           released BOOLEAN
);
CREATE TABLE data_model_info (
                        code VARCHAR(255) PRIMARY KEY,
                        description VARCHAR(255),
                        datasetLocations VARCHAR(255),
                        records INT
);
