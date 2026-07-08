ALTER TABLE room_layout
    ADD CONSTRAINT uq_room_layout_room_id UNIQUE (room_id);
