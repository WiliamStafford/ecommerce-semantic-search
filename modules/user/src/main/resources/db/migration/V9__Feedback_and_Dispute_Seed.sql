-- Đánh giá cho order_item_id 100 và 101
INSERT INTO reviews (user_id, order_item_id, rating, comment) VALUES
                                                                  (7, 100, 1, 'Serum gây dị ứng da, rất thất vọng!'),
                                                                  (8, 101, 5, 'Lego lắp rất sướng, shop giao nhanh.');

-- Khiếu nại cho đơn 100
INSERT INTO return_requests (order_item_id, customer_id, seller_id, return_reason, status)
VALUES (100, 7, 32, 'QUALITY_NOT_GOOD', 'PENDING');
SET @ret_id = LAST_INSERT_ID();

-- Duyệt hoàn tiền
INSERT INTO return_refunds (return_request_id, refund_amount, refund_status) VALUES (@ret_id, 450000, 'SUCCESS');
UPDATE seller_wallet SET balance = balance - 450000 WHERE user_id = 32;