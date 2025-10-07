DELETE FROM item_image;

ALTER TABLE item_image RENAME COLUMN caminho_image TO content_type;