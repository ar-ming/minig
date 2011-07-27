DROP TABLE IF EXISTS minig_filters;
DROP TABLE IF EXISTS minig_signature;
DROP TABLE IF EXISTS minig_uids;
DROP TABLE IF EXISTS minig_available_folders;
DROP TABLE IF EXISTS minig_subscribed_folders;
DROP TABLE IF EXISTS minig_cache;

CREATE TABLE minig_cache (
	id SERIAL PRIMARY KEY,
	user_id VARCHAR(255) NOT NULL			
);

CREATE INDEX idx_cache_user_id ON minig_cache(user_id);

CREATE TABLE minig_subscribed_folders (
	id SERIAL PRIMARY KEY, 
	name TEXT NOT NULL, 
	display_name TEXT NOT NULL,
	shared BOOLEAN NOT NULL,
	minig_cache INTEGER REFERENCES minig_cache(id) ON DELETE CASCADE
);

CREATE INDEX idx_subscribed_folders_cache ON minig_subscribed_folders(minig_cache);

CREATE TABLE minig_available_folders (
	id SERIAL PRIMARY KEY, 
	name TEXT NOT NULL, 
	display_name TEXT NOT NULL,
	subscribed  BOOLEAN NOT NULL,
	shared BOOLEAN NOT NULL,
	minig_cache INTEGER REFERENCES minig_cache(id) ON DELETE CASCADE
);

CREATE INDEX idx_available_folders_cache ON minig_available_folders (minig_cache);

CREATE TABLE minig_uids (
	folder_id INTEGER REFERENCES minig_subscribed_folders(id) ON DELETE CASCADE,
	uid INTEGER NOT NULL,
	minig_cache INTEGER REFERENCES minig_cache(id) ON DELETE CASCADE
);

CREATE INDEX idx_uids_folder_id ON minig_uids(folder_id);
CREATE INDEX idx_uids_cache ON minig_uids(minig_cache);

CREATE TABLE minig_signature (
	email VARCHAR(255) NOT NULL CONSTRAINT signature_pk PRIMARY KEY, 
	signature TEXT NOT NULL,
	minig_cache INTEGER REFERENCES minig_cache(id) ON DELETE CASCADE
);

CREATE TABLE minig_filters (
	id SERIAL PRIMARY KEY, 
	criteria TEXT NOT NULL, 
	star BOOLEAN NOT NULL,
	mark_read BOOLEAN NOT NULL,
	delete_it BOOLEAN NOT NULL,
	forward VARCHAR(255),
	deliver VARCHAR(255),
	minig_cache INTEGER REFERENCES minig_cache(id) ON DELETE CASCADE
);
