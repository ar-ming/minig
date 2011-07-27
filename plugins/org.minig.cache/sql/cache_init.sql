CREATE TABLE minig_cache (
	id SMALLINT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT minig_cache_pk PRIMARY KEY,
	user_id VARCHAR(255) NOT NULL			
);

CREATE INDEX idx_cache_user_id ON minig_cache (user_id);

CREATE TABLE minig_subscribed_folders (
	id SMALLINT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT subscribed_folders_pk PRIMARY KEY, 
	name VARCHAR(255) NOT NULL, 
	display_name VARCHAR(80) NOT NULL,
	minig_cache SMALLINT  REFERENCES minig_cache ON DELETE CASCADE
);

CREATE INDEX idx_subscribed_folders_cache ON minig_subscribed_folders (minig_cache);

CREATE TABLE minig_available_folders (
	id SMALLINT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT available_folders_pk PRIMARY KEY, 
	name VARCHAR(255) NOT NULL, 
	display_name VARCHAR(80) NOT NULL,
	subscribed  SMALLINT NOT NULL,
	minig_cache SMALLINT   REFERENCES minig_cache ON DELETE CASCADE
);

CREATE INDEX idx_available_folders_cache ON minig_available_folders (minig_cache);

CREATE TABLE minig_uids (
	folder_id SMALLINT CONSTRAINT uids_folders_fk REFERENCES minig_subscribed_folders ON DELETE CASCADE,
	uid INT NOT NULL,
	seen SMALLINT NOT NULL,
	minig_cache SMALLINT   REFERENCES minig_cache ON DELETE CASCADE
);

CREATE INDEX idx_uids_folder_id ON minig_uids (folder_id);
CREATE INDEX idx_uids_cache ON minig_uids (minig_cache);

CREATE TABLE minig_signature (
	email VARCHAR(255) NOT NULL CONSTRAINT signature_pk PRIMARY KEY, 
	signature LONG VARCHAR NOT NULL,
	minig_cache SMALLINT   REFERENCES minig_cache ON DELETE CASCADE
);

CREATE TABLE minig_filters (
	id SMALLINT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT filters_pk PRIMARY KEY, 
	criteria LONG VARCHAR NOT NULL, 
	star SMALLINT NOT NULL,
	mark_read SMALLINT NOT NULL,
	delete_it SMALLINT NOT NULL,
	forward VARCHAR(255),
	deliver VARCHAR(255),
	minig_cache SMALLINT   REFERENCES minig_cache ON DELETE CASCADE
);
