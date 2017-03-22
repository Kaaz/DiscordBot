ALTER TABLE users
  ADD last_currency_retrieval INT DEFAULT 0 NOT NULL;
ALTER TABLE bank_transactions
  ADD amount INT NOT NULL;
CREATE INDEX bank_transactions_bank_from_index
  ON bank_transactions (bank_from);
CREATE INDEX bank_transactions_bank_to_index
  ON bank_transactions (bank_to);
CREATE INDEX bank_transactions_bank_from_bank_to_index
  ON bank_transactions (bank_from, bank_to);

CREATE TABLE bot_versions (
  id         INT PRIMARY KEY AUTO_INCREMENT,
  major      INT NOT NULL,
  minor      INT NOT NULL,
  patch      INT NOT NULL,
  created_on INT
);
CREATE UNIQUE INDEX bot_versions_major_minor_patch_uindex
  ON bot_versions (major, minor, patch);
CREATE TABLE bot_version_changes (
  id          INT PRIMARY KEY AUTO_INCREMENT,
  version     INT NOT NULL,
  change_type INT,
  description VARCHAR(128),
  author      INT
);
CREATE INDEX bot_version_changes_version_index
  ON bot_version_changes (version);
ALTER TABLE bot_versions
  MODIFY created_on TIMESTAMP;
ALTER TABLE bot_versions
  ADD published INT DEFAULT 0 NULL;