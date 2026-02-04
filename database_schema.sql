CREATE TABLE community (
    community_id uuid PRIMARY KEY,
    name text,
    handle character varying(256),
    n_items integer,
    n_bitstreams integer,
    n_bytes bigint
);

CREATE TABLE collection (
    collection_id uuid PRIMARY KEY,
    name text,
    handle character varying(256),
    n_items integer,
    n_bitstreams integer,
    n_bytes bigint
);

CREATE TABLE item (
    item_id uuid PRIMARY KEY,
    name text,
    handle character varying(256),
    n_items integer,
    n_bitstreams integer,
    n_bytes bigint
);

CREATE TABLE community2community (
    parent_comm_id uuid REFERENCES community(community_id),
    child_comm_id uuid REFERENCES community(community_id)
);

CREATE TABLE community2collection (
    community_id uuid REFERENCES community(community_id),
    collection_id uuid REFERENCES collection(collection_id)
);

CREATE TABLE collection2item (
    collection_id uuid REFERENCES collection(collection_id),
    item_id uuid REFERENCES item(item_id)
);

CREATE TABLE item2bitstream (
    item_id uuid REFERENCES item(item_id),
    bitstream_id uuid
);

CREATE TABLE downloadspercommunity (
    community_id uuid REFERENCES community(community_id),
    "time" integer NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    PRIMARY KEY(community_id, "time")
);

CREATE TABLE downloadspercollection (
    collection_id uuid REFERENCES collection(collection_id),
    "time" integer NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    PRIMARY KEY(collection_id, "time")
);


CREATE TABLE downloadsperitem (
    item_id uuid REFERENCES item(item_id),
    "time" integer NOT NULL,
    count integer DEFAULT 0 NOT NULL,
    PRIMARY KEY(item_id, "time")
);

CREATE INDEX downloadsperitem_count_idx ON downloadsperitem USING btree (count);

CREATE INDEX item_handle_idx ON item USING btree (handle);
CREATE INDEX collection_handle_idx ON collection USING btree (handle);
CREATE INDEX community_handle_idx ON community USING btree (handle);
