CREATE TABLE merchant
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    apiKey     VARCHAR(2048),
    secret_key VARCHAR(2048),
    created_at DATE,
    updated_at DATE,
    status     VARCHAR(50)
);

CREATE TABLE customer
(
    id         SERIAL PRIMARY KEY,
    firstname  VARCHAR(50),
    lastname   VARCHAR(50),
    created_at DATE,
    updated_at DATE,
    status     VARCHAR(50),
    country    VARCHAR(50)
);

CREATE TABLE account
(
    id          SERIAL PRIMARY KEY,
    customer_id INTEGER,
    merchant_id INTEGER,
    owner_type  VARCHAR(50) CHECK (owner_type IN ('customer', 'merchant')),
    currency    VARCHAR(50),
    balance     DECIMAL(18, 2),
    created_at  DATE,
    updated_at  DATE,
    status      VARCHAR(50),
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_merchant FOREIGN KEY (merchant_id) REFERENCES merchant (id),
    CONSTRAINT check_owner CHECK (
        (customer_id IS NOT NULL AND merchant_id IS NULL AND owner_type = 'customer') OR
        (merchant_id IS NOT NULL AND customer_id IS NULL AND owner_type = 'merchant')
        )
);


CREATE TABLE card
(
    id          SERIAL PRIMARY KEY,
    account_id  INTEGER,
    card_number BIGINT,
    exp_date    DATE,
    cvv         VARCHAR(4),
    created_at  DATE,
    updated_at  DATE,
    status      VARCHAR(50),
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE transaction
(
    id              SERIAL PRIMARY KEY,
    card_id         INTEGER,
    account_id      INTEGER,
    amount          DECIMAL(18, 2),
    currency        VARCHAR(50),
    status          VARCHAR(50),
    message         VARCHAR(2048),
    notification_url VARCHAR(2048),
    created_at      DATE,
    updated_at      DATE,
    language        VARCHAR(50),
    operation_type  VARCHAR(50),
    CONSTRAINT fk_card FOREIGN KEY (card_id) REFERENCES card (id),
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE webhook
(
    id                SERIAL PRIMARY KEY,
    transaction_id    INTEGER,
    notification_url  VARCHAR(2048),
    status            VARCHAR(50),
    attempts          INTEGER,
    last_attempt_time TIMESTAMP,
    created_at        DATE,
    updated_at        DATE,
    response_status   VARCHAR(50),
    response_body     VARCHAR(50),
    CONSTRAINT fk_transaction FOREIGN KEY (transaction_id) REFERENCES transaction (id)
);
