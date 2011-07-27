DELETE from minig_available_folders;
DELETE from minig_subscribed_folders;
ALTER TABLE minig_available_folders ADD COLUMN shared BOOLEAN NOT NULL;
ALTER TABLE minig_subscribed_folders ADD COLUMN shared BOOLEAN NOT NULL;
