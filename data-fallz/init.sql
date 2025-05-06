CREATE SEQUENCE Task_sequence
   START WITH 1
   INCREMENT BY 1;

CREATE TABLE List_user(
   id_user UUID,
   username VARCHAR(50) UNIQUE,
   "password" TEXT,
   mail VARCHAR(255) UNIQUE,  
   PRIMARY KEY(id_user)
);

CREATE TABLE List(
   id_owner UUID,
   "name" VARCHAR(50),
   id_list UUID,
   PRIMARY KEY(id_list),
   FOREIGN KEY (id_owner) REFERENCES List_user(id_user)
);

CREATE TABLE Task(
   id_task UUID,
   id_list UUID,
   "name" VARCHAR(50),
   "description" VARCHAR(255),
   assignee_id UUID,
   created_date TIMESTAMP,
   PRIMARY KEY(id_task),
   FOREIGN KEY (id_list) REFERENCES List(id_list),
   FOREIGN KEY (assignee_id) REFERENCES List_user(id_user)
);