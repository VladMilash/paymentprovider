CREATE TABLE merchant
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    apiKey     VARCHAR(2048),
    secret_key VARCHAR(2048),
    createdAt  DATE,
    updatedAt  DATE,
    status     VARCHAR(50)
);

CREATE TABLE customer
(
    id        SERIAL PRIMARY KEY,
    firstname VARCHAR(50),
    lastname  VARCHAR(50),
    createdAt DATE,
    updatedAt DATE,
    status    VARCHAR(50),
    country   VARCHAR(50)
);

CREATE TABLE account
(
    id          SERIAL PRIMARY KEY,
    customer_id INTEGER,
    merchant_id INTEGER,
    owner_type  VARCHAR(50) CHECK (owner_type IN ('customer', 'merchant')),
    currency    VARCHAR(50),
    balance     BIGINT,
    createdAt   DATE,
    updatedAt   DATE,
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
    id         SERIAL PRIMARY KEY,
    accountId  INTEGER,
    cardNumber INTEGER,
    expDate    DATE,
    cvv        INTEGER,
    createdAt  DATE,
    updatedAt  DATE,
    status     VARCHAR(50),
    CONSTRAINT fk_account FOREIGN KEY (accountId) REFERENCES account (id)
);

CREATE TABLE transaction
(
    id              SERIAL PRIMARY KEY,
    cardId          INTEGER,
    accountId       INTEGER,
    amount          INTEGER,
    currency        VARCHAR(50),
    status          VARCHAR(50),
    message         VARCHAR(2048),
    notificationUrl VARCHAR(2048),
    createdAt       DATE,
    updatedAt       DATE,
    language        VARCHAR(50),
    operationType   VARCHAR(50),
    CONSTRAINT fk_card FOREIGN KEY (cardId) REFERENCES card (id),
    CONSTRAINT fk_account FOREIGN KEY (accountId) REFERENCES account (id)
);

CREATE TABLE webhook
(
    id                SERIAL PRIMARY KEY,
    transactionID     INTEGER,
    notificationUrl   VARCHAR(50),
    status            VARCHAR(50),
    attempts          INTEGER,
    last_attempt_time TIME,
    createdAt         DATE,
    updatedAt         DATE,
    response_status   VARCHAR(50),
    response_body     VARCHAR(50),
    CONSTRAINT fk_transaction FOREIGN KEY (transactionID) REFERENCES transaction (id)
)


