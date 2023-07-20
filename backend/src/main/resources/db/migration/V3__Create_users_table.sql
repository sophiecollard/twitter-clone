CREATE TABLE users (
  id            TEXT PRIMARY KEY,
  handle        TEXT NOT NULL,
  name          TEXT NOT NULL,
  status        TEXT NOT NULL,
  registered_on TIMESTAMP NOT NULL
);
