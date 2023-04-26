CREATE TABLE tweet_likes (
    tweet_id TEXT NOT NULL,
    user_id  TEXT NOT NULL,
    CONSTRAINT tweek_likes_pkey PRIMARY KEY (tweet_id, user_id)
);
