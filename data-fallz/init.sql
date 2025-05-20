CREATE TABLE "user" (
    id_user UUID,
    "password" VARCHAR(255),
    mail VARCHAR(255) UNIQUE,
    PRIMARY KEY(id_user)
);

CREATE TABLE Person(
    id_person UUID,
    id_user UUID,
    firstname VARCHAR(50),
    lastname VARCHAR(50),
    PRIMARY KEY (id_person),
    FOREIGN KEY (id_user) REFERENCES "user"(id_user)
);

CREATE TABLE Device(
   id_device UUID,
   id_person UUID,
   PRIMARY KEY(id_device),
   FOREIGN KEY (id_person) REFERENCES Person(id_person)
);

CREATE TABLE Paiement(
    id_paiement UUID,
    id_user UUID,
    amount NUMERIC(10,2),
    date TIMESTAMP,
    PRIMARY KEY(id_paiement),
    FOREIGN KEY (id_user) REFERENCES "user"(id_user)
);

CREATE TABLE Parcours(
    id_parcours UUID,
    id_device UUID,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    PRIMARY KEY(id_parcours),
    FOREIGN KEY (id_device) REFERENCES Device(id_device)
);

CREATE TABLE  Coordonates(
                             id_coordonates UUID,
                             id_parcours UUID,
                             latitude NUMERIC(15,8),
                             longitude NUMERIC(15,8),
                             date TIMESTAMP,
                             PRIMARY KEY(id_coordonates),
                             FOREIGN KEY (id_parcours) REFERENCES Parcours(id_parcours)
);

CREATE TABLE Fall(
    id_fall UUID,
    id_coordonates UUID,
    id_person UUID,
    PRIMARY KEY(id_fall),
    FOREIGN KEY (id_coordonates) REFERENCES Coordonates(id_coordonates),
    FOREIGN KEY (id_person) REFERENCES Person(id_person)
);
