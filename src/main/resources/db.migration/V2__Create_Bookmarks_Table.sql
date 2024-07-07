CREATE TABLE bookmarks(
  id UUID PRIMARY KEY,
  identifier VARCHAR(128) NOT NULL,
  comment VARCHAR(1024),
  provider VARCHAR(32) NOT NULL DEFAULT 'arxiv',
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  user_id UUID REFERENCES users(id) NOT NULL,
  CONSTRAINT index_on_bookmarks_provider_and_identifier UNIQUE(provider, identifier)
);
