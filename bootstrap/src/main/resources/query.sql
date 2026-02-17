CREATE TABLE document_types (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                code VARCHAR(50) NOT NULL UNIQUE,
                                name VARCHAR(150) NOT NULL,
                                description TEXT,
                                active BOOLEAN NOT NULL DEFAULT true,
                                search_key NOT NULL  VARCHAR(100),
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE insurances (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            name VARCHAR(150) NOT NULL,
                            policy_number VARCHAR(100) NOT NULL UNIQUE,
                            type VARCHAR(50) NOT NULL,
                            active BOOLEAN NOT NULL DEFAULT true,
                            search_key NOT NULL  VARCHAR(100),
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medical_centers (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 nit VARCHAR(50) NOT NULL UNIQUE,
                                 center_type VARCHAR(50) NOT NULL,
                                 name VARCHAR(150) NOT NULL,
                                 email VARCHAR(150),
                                 phone VARCHAR(50),
                                 description TEXT,
                                 address VARCHAR(255),
                                 country VARCHAR(100),
                                 city VARCHAR(100),
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medical_center_branches (
                                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                         medical_center_id UUID NOT NULL,
                                         name VARCHAR(150) NOT NULL,
                                         address VARCHAR(255) NOT NULL,
                                         city VARCHAR(100) NOT NULL,
                                         country VARCHAR(100) NOT NULL,
                                         phone VARCHAR(50),
                                         email VARCHAR(150),
                                         active BOOLEAN NOT NULL DEFAULT true,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                         CONSTRAINT fk_branch_center
                                             FOREIGN KEY (medical_center_id)
                                                 REFERENCES medical_centers(id)
                                                 ON DELETE CASCADE
);

CREATE TABLE specialties (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             name VARCHAR(150) NOT NULL,
                             active BOOLEAN NOT NULL DEFAULT true,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE medical_services (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  code VARCHAR(50) NOT NULL UNIQUE,
                                  name VARCHAR(150) NOT NULL,
                                  active BOOLEAN NOT NULL DEFAULT true,
                                  search_key VARCHAR(100) NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE specialty_service(
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  specialty_id UUID NOT NULL,
                                  medical_service_id UUID NOT NULL,
                                  duration INTEGER NOT NULL,
                                  price DECIMAL(10, 2) NOT NULL,
                                  search_key VARCHAR(100) NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

                              constraint fk_specialty
                                  foreign key (specialty_id)
                                      references specialties(id)
                                      on delete cascade,

                                constraint fk_medical_service
                                  foreign key (medical_service_id)
                                      references medical_services(id)
                                      on delete cascade


)
