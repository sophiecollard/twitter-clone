CREATE TABLE tweets (
  id        TEXT PRIMARY KEY,
  author_id TEXT NOT NULL,
  contents  TEXT NOT NULL,
  posted_on TIMESTAMP NOT NULL
);
