CREATE TABLE comments (
  id        TEXT PRIMARY KEY,
  author_id TEXT NOT NULL,
  tweet_id  TEXT NOT NULL,
  contents  TEXT NOT NULL,
  posted_on TIMESTAMP NOT NULL
);
