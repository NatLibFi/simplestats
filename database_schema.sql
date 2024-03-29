CREATE TABLE community (
    community_id integer PRIMARY KEY,
    name text,
    handle character varying(256),
    n_items integer,
    n_bitstreams integer,
    n_bytes bigint
);

CREATE TABLE collection (
    collection_id integer PRIMARY KEY,
    name text,
    handle character varying(256),
    n_items integer,
    n_bitstreams integer,
    n_bytes bigint
);

CREATE TABLE item (
    item_id integer PRIMARY KEY,
    name text,
    handle character varying(256),
    n_items integer,
    n_bitstreams integer,
    n_bytes bigint
);


CREATE TABLE community2community (
    parent_comm_id integer REFERENCES community(community_id),
    child_comm_id integer REFERENCES community(community_id)
);

CREATE TABLE community2collection (
    community_id integer REFERENCES community(community_id),
    collection_id integer REFERENCES collection(collection_id)
);

CREATE TABLE collection2item (
    collection_id integer REFERENCES collection(collection_id),
    item_id integer REFERENCES item(item_id)
);

CREATE TABLE item2bitstream (
    item_id integer REFERENCES item(item_id),
    bitstream_id integer
);

CREATE TABLE downloadspercommunity (
    community_id integer REFERENCES community(community_id),
    "time" integer NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    PRIMARY KEY(community_id, "time")
);

CREATE TABLE downloadspercollection (
    collection_id integer REFERENCES collection(collection_id),
    "time" integer NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    PRIMARY KEY(collection_id, "time")
);

CREATE TABLE downloadsperitem (
    item_id integer REFERENCES item(item_id),
    "time" integer NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    PRIMARY KEY(item_id, "time")
);

-- This *might* make things faster.
CREATE INDEX downloadsperitem_count_idx ON downloadsperitem (count);

CREATE index item_handle_idx ON item (handle);
CREATE index collection_handle_idx ON collection (handle);
CREATE index community_handle_idx ON community (handle);
