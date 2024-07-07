CREATE TABLE subscriptions(
  id UUID PRIMARY KEY,
  keyword VARCHAR(128) NOT NULL,
  notification_period_in_days INT NOT NULL DEFAULT(7),
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  last_checked_at TIMESTAMP WITHOUT TIME ZONE,
  user_id UUID REFERENCES users(id) NOT NULL,
  CONSTRAINT index_on_subscriptions_user_id_and_keyword UNIQUE(user_id, keyword)
);
