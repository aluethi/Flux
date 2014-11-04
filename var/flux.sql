CREATE TABLE client
(
    id integer NOT NULL,
    created timestamp without time zone DEFAULT now(),
    CONSTRAINT client_pkey PRIMARY KEY (id)
);
CREATE INDEX client_idx_id ON client (id);

CREATE SEQUENCE message_id_seq;
CREATE TABLE message
(
    id integer PRIMARY KEY DEFAULT nextval('message_id_seq'::regclass),
    sender int NOT NULL,
    receiver int,
    queue int,
    priority int,
    created timestamp without time zone DEFAULT now(),
    content text
);
CREATE INDEX message_idx_id ON message (id);
CREATE INDEX message_idx_sender ON message (sender);
CREATE INDEX message_idx_receiver ON message (receiver);
CREATE INDEX message_idx_queue ON message (queue);
CREATE INDEX message_idx_priority ON message (priority);
CREATE INDEX message_idx_created ON message (created);

CREATE SEQUENCE queue_id_seq;
CREATE TABLE queue
(
    id integer NOT NULL DEFAULT nextval('queue_id_seq'::regclass),
    handle char(100),
    created timestamp without time zone DEFAULT now(),
    CONSTRAINT queue_pkey PRIMARY KEY (id)
);
CREATE INDEX queue_idx_id ON queue (id);
CREATE INDEX queue_idx_handle ON queue (handle);


-----------------
-- regsiter client
-----------------
CREATE OR REPLACE FUNCTION registerClient(integer)
	RETURNS void AS
$BODY$
declare
begin
	INSERT INTO client (id) VALUES ($1);
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


-----------------
-- deregsiter client
-----------------
CREATE OR REPLACE FUNCTION deregisterClient(integer)
	RETURNS void AS
$BODY$
declare
begin
    IF EXISTS(SELECT 1 FROM client WHERE id = $1) THEN
	    DELETE FROM client WHERE id = $1;
	ELSE
	    RAISE 'No client found with id %.', $1 USING ERRCODE = 'F0001';
	END IF;
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- create queue
----------------
CREATE OR REPLACE FUNCTION createQueue(character varying)
	RETURNS queue AS
$BODY$
declare
    result_set queue;
begin
    IF EXISTS(SELECT 1 FROM queue WHERE handle = $1) THEN
        RAISE 'Queue with handle % already exists.', $1 USING ERRCODE = 'F0002';
    ELSE
        INSERT INTO queue (handle) VALUES ($1) RETURNING id, handle, created INTO result_set;
        return result_set;
    END IF;
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- get queue
----------------
CREATE OR REPLACE FUNCTION getQueue(character varying)
	RETURNS queue AS
$BODY$
declare
    result_set queue;
begin
    IF EXISTS(SELECT 1 FROM queue WHERE handle = $1) THEN
        SELECT id, handle, created FROM queue WHERE handle = $1 INTO result_set;
        return result_set;
    ELSE
	    RAISE 'No queue found with handle %.', $1 USING ERRCODE = 'F0003';
    END IF;
end
$BODY$
LANGUAGE plpgsql STABLE
COST 100;


----------------
-- delete queue
----------------
CREATE OR REPLACE FUNCTION deleteQueue(character varying)
	RETURNS void AS
$BODY$
declare
begin
    IF EXISTS(SELECT 1 FROM queue WHERE handle = $1) THEN
	    DELETE FROM queue WHERE handle = $1;
	ELSE
	    RAISE 'No queue found with handle %.', $1 USING ERRCODE = 'F0003';
	END IF;
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- is queue empty
----------------
CREATE OR REPLACE FUNCTION isQueueEmpty(character varying)
	RETURNS boolean AS
$BODY$
declare
    result boolean;
    temp integer;
begin
    IF EXISTS(SELECT 1 FROM queue WHERE handle = $1) THEN
        SELECT id FROM queue WHERE handle = $1 INTO temp;
        SELECT CASE
            WHEN EXISTS(SELECT 1 FROM message WHERE queue = temp) THEN FALSE
            ELSE TRUE
        END INTO result;
        return result;
    ELSE
	    RAISE 'No queue found with handle %.', $1 USING ERRCODE = 'F0003';
    END IF;
end
$BODY$
LANGUAGE plpgsql STABLE
COST 100;


----------------
-- query for queues
----------------
CREATE OR REPLACE FUNCTION queryForQueues()
	RETURNS queue AS
$BODY$
declare
    result_set queue;
begin
    SELECT queue.id, queue.handle, queue.created
    FROM queue, message
    WHERE queue.id = message.queue
    GROUP BY queue.id INTO result_set;
    return result_set;
end
$BODY$
LANGUAGE plpgsql STABLE
COST 100;


----------------
-- query for queues from sender
----------------
CREATE OR REPLACE FUNCTION queryForQueuesFromSender(integer)
	RETURNS queue AS
$BODY$
declare
    result_set queue;
begin
    SELECT queue.id, queue.handle, queue.created
    FROM queue, message
    WHERE queue.id = message.queue and message.sender = $1
    GROUP BY queue.id INTO result_set;
    return result_set;
end
$BODY$
LANGUAGE plpgsql STABLE
COST 100;


----------------
-- enqueue message
----------------
CREATE OR REPLACE FUNCTION enqueueMessage(queueHandle character varying, msgSender integer, msgReceiver integer, msgPriority integer, msgContent text)
	RETURNS void AS
$BODY$
declare
    temp integer;
begin
    IF EXISTS(SELECT 1 FROM client WHERE id = msgSender) THEN
        IF EXISTS(SELECT 1 FROM queue WHERE handle = queueHandle) THEN
            SELECT id FROM queue WHERE handle = queueHandle INTO temp;
            INSERT INTO message(sender, receiver, queue, priority, content) VALUES(msgSender, msgReceiver, temp, msgPriority, msgContent);
        ELSE
	        RAISE 'No queue found with handle %.', queueHandle USING ERRCODE = 'F0003';
	    END IF;
	ELSE
	    RAISE 'No client found with id %.', msgSender USING ERRCODE = 'F0001';
	END IF;
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- dequeue message
----------------
CREATE OR REPLACE FUNCTION dequeueMessage(queueHandle character varying, receiverId integer)
	RETURNS message AS
$BODY$
declare
    temp integer;
    result_set message;
begin
    IF EXISTS(SELECT 1 FROM client WHERE id = receiverId) THEN
        IF EXISTS(SELECT 1 FROM queue WHERE handle = queueHandle) THEN
            SELECT id FROM queue WHERE handle = queueHandle INTO temp;
            IF EXISTS(SELECT 1 FROM message WHERE message.queue = temp AND message.receiver = receiverId) THEN
                SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                FROM message WHERE message.queue = temp AND message.receiver = receiverId ORDER BY message.created ASC LIMIT 1 INTO result_set;
            ELSE
                SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                FROM message WHERE message.queue = temp AND message.receiver = 0 ORDER BY message.created ASC LIMIT 1 INTO result_set;
            END IF;
            DELETE FROM message WHERE message.id = result_set.id;
            return result_set;
        ELSE
            RAISE 'No queue found with handle %.', queueHandle USING ERRCODE = 'F0003';
        END IF;
    END IF;
    RAISE 'No client found with id %.', receiverId USING ERRCODE = 'F0001';
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- dequeue message from sender
----------------
CREATE OR REPLACE FUNCTION dequeueMessageFromSender(queueHandle character varying, senderId integer, receiverId integer)
	RETURNS message AS
$BODY$
declare
    temp integer;
    result_set message;
begin
    IF EXISTS(SELECT 1 FROM client WHERE id = receiverId) THEN
        IF EXISTS(SELECT 1 FROM client WHERE id = senderId) THEN
            IF EXISTS(SELECT 1 FROM queue WHERE handle = queueHandle) THEN
                SELECT id FROM queue WHERE handle = queueHandle INTO temp;
                IF EXISTS(SELECT 1 FROM message WHERE message.queue = temp AND message.receiver = receiverId AND message.sender = senderId) THEN
                    SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                    FROM message WHERE message.queue = temp AND message.sender = senderId AND message.receiver = receiverId ORDER BY message.created ASC LIMIT 1 INTO result_set;
                ELSE
                    SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                    FROM message WHERE message.queue = temp AND message.sender = senderId AND message.receiver = 0 ORDER BY message.created ASC LIMIT 1 INTO result_set;
                END IF;
                DELETE FROM message WHERE message.id = result_set.id;
                return result_set;
            ELSE
                RAISE 'No queue found with handle %.', queueHandle USING ERRCODE = 'F0003';
            END IF;
        ELSE
            RAISE 'No client found with id %.', senderId USING ERRCODE = 'F0001';
        END IF;
    ELSE
        RAISE 'No client found with id %.', receiverId USING ERRCODE = 'F0001';
    END IF;
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- peek message
----------------
CREATE OR REPLACE FUNCTION peekMessage(queueHandle character varying, receiverId integer)
	RETURNS message AS
$BODY$
declare
    temp integer;
    result_set message;
begin
    IF EXISTS(SELECT 1 FROM client WHERE id = receiverId) THEN
        IF EXISTS(SELECT 1 FROM queue WHERE handle = queueHandle) THEN
            SELECT id FROM queue WHERE handle = queueHandle INTO temp;
            IF EXISTS(SELECT 1 FROM message WHERE message.queue = temp AND message.receiver = receiverId) THEN
                SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                FROM message WHERE message.queue = temp AND message.receiver = receiverId ORDER BY message.created ASC LIMIT 1 INTO result_set;
            ELSE
                SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                FROM message WHERE message.queue = temp AND message.receiver = 0 ORDER BY message.created ASC LIMIT 1 INTO result_set;
            END IF;
            return result_set;
        ELSE
            RAISE 'No queue found with handle %.', queueHandle USING ERRCODE = 'F0003';
        END IF;
    END IF;
    RAISE 'No client found with id %.', receiverId USING ERRCODE = 'F0001';
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;


----------------
-- peek message from sender
----------------
CREATE OR REPLACE FUNCTION peekMessageFromSender(queueHandle character varying, senderId integer, receiverId integer)
	RETURNS message AS
$BODY$
declare
    temp integer;
    result_set message;
begin
    IF EXISTS(SELECT 1 FROM client WHERE id = receiverId) THEN
        IF EXISTS(SELECT 1 FROM client WHERE id = senderId) THEN
            IF EXISTS(SELECT 1 FROM queue WHERE handle = queueHandle) THEN
                SELECT id FROM queue WHERE handle = queueHandle INTO temp;
                IF EXISTS(SELECT 1 FROM message WHERE message.queue = temp AND message.receiver = receiverId AND message.sender = senderId) THEN
                    SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                    FROM message WHERE message.queue = temp AND message.sender = senderId AND message.receiver = receiverId ORDER BY message.created ASC LIMIT 1 INTO result_set;
                ELSE
                    SELECT message.id, message.sender, message.receiver, message.queue, message.priority, message.created, message.content
                    FROM message WHERE message.queue = temp AND message.sender = senderId AND message.receiver = 0 ORDER BY message.created ASC LIMIT 1 INTO result_set;
                END IF;
                DELETE FROM message WHERE message.id = result_set.id;
                return result_set;
            ELSE
                RAISE 'No queue found with handle %.', queueHandle USING ERRCODE = 'F0003';
            END IF;
        ELSE
            RAISE 'No client found with id %.', senderId USING ERRCODE = 'F0001';
        END IF;
    ELSE
        RAISE 'No client found with id %.', receiverId USING ERRCODE = 'F0001';
    END IF;
end
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;