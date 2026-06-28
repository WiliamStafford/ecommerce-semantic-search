INSERT INTO order_items (order_id, seller_product_id, quantity, price, product_name, image_url)
SELECT
    o.id,
    p.id as seller_product_id,
    FLOOR(RAND() * 3) + 1 as quantity,
    p.price,
    p.product_name,
    p.image_url
FROM orders o
         CROSS JOIN seller_products p
WHERE p.id IN (1, 5, 10, 17, 18, 19, 25, 30, 31, 36)
  AND RAND() < 0.3;