CREATE TABLE followers (
    follower_id TEXT NOT NULL,
    followee_id TEXT NOT NULL,
    CONSTRAINT followers_pk PRIMARY KEY(follower_id, followee_id)
);
