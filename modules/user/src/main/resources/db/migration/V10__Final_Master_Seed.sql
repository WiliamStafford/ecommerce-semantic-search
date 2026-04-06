UPDATE users SET avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=Hung' WHERE id = 6;
UPDATE users SET avatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=Beauty' WHERE id = 32;
-- Cộng tiền đơn Lego đã giao thành công
UPDATE seller_wallet SET balance = balance + 1200000 WHERE user_id = 33;