USE `zimbra`;

CREATE TABLE IF NOT EXISTS mail_history_mbox
(
	logtime		timestamp   NOT NULL,
    message_id	text	NOT NULL,
    from_domain	text	NOT NULL,
    from_localpart	text	NOT NULL,
    to_domain	text	NOT NULL,
    to_localpart	text	NOT NULL,
	moveingId	text	NOT NULL,
	movinginfoid	text NOT NULL,
    event	text	NOT NULL,
	foldername text NOT NULL

);

CREATE INDEX  mail_history_mbox_i1 ON mail_history_mbox (message_id(30));
CREATE INDEX  mail_history_mbox_i2 ON mail_history_mbox (logtime);
CREATE INDEX  mail_history_mbox_i3 ON mail_history_mbox (message_id(30), logtime);


CREATE VIEW  mail_log_internal AS
    SELECT
    logtime,
    message_id,
    from_domain,
    from_localpart,
    to_domain,
    to_localpart,
	foldername,
    event
    FROM
    mail_history_mbox;

