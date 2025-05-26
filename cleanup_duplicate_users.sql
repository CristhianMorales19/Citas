-- First, identify duplicate usernames
SELECT username, COUNT(*) as count 
FROM users 
GROUP BY username 
HAVING COUNT(*) > 1;

-- Then, for each duplicate username, keep one record and delete the rest
-- Replace 'duplicate_username' with the actual duplicate usernames from the query above
-- For example, to handle duplicates for username 'admin':
-- DELETE FROM users 
-- WHERE username = 'admin' 
-- AND id NOT IN (
--     SELECT MIN(id) 
--     FROM users 
--     WHERE username = 'admin'
-- );

-- After cleaning up, add a unique constraint to prevent future duplicates
-- ALTER TABLE users ADD CONSTRAINT uk_username UNIQUE (username);
