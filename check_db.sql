SELECT p.id, p.name, u.email, sp.id as seller_id 
FROM product p 
JOIN seller_profile sp ON p.seller_profile_id = sp.id 
JOIN user u ON sp.user_id = u.id 
LIMIT 5;
