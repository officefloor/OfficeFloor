CREATE TABLE MESSAGE (
  ID BIGINT IDENTITY PRIMARY KEY,
  CONTENT VARCHAR(50)
);

INSERT INTO MESSAGE ( CONTENT ) VALUES ( 'TEST' );
