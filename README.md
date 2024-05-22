## Table for receipt

CREATE TABLE receipt (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    date DATE,
    time VARCHAR(20),
    address VARCHAR(255),
    orderId VARCHAR(50),
    paymentMethod VARCHAR(50),
    deliveryMethod VARCHAR(50)
);

ALTER TABLE receipt
ADD COLUMN cart_items TEXT,
ADD COLUMN total_amount DOUBLE

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    productName VARCHAR(255),
    price DOUBLE,
    category VARCHAR(255),
    imageName VARCHAR(255), 
    imageData BLOB
);

ALTER TABLE products MODIFY COLUMN imageData LONGBLOB;

CREATE TABLE lastUpdated (
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updateText VARCHAR(255)
);

INSERT INTO lastupdatedtable (last_updated, updateText) VALUES (CURRENT_TIMESTAMP, "update");
