CREATE TABLE notification (
       status INTEGER NOT NULL,
       mid INTEGER NOT NULL,
       minig_cache INTEGER REFERENCES minig_cache(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_notification ON notification (mid, minig_cache);