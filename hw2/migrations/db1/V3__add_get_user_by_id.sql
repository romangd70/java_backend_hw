CREATE OR REPLACE FUNCTION get_user_by_id(user_id BIGINT)
RETURNS TABLE(id BIGINT, username VARCHAR, email VARCHAR, created_at TIMESTAMP) AS
$$
BEGIN
    RETURN QUERY
    SELECT u.id, u.username, u.email, u.created_at
    FROM users u
    WHERE u.id = user_id;
END;
$$ LANGUAGE plpgsql;
