-- Insert API tokens for health institutions
INSERT IGNORE INTO api_tokens (token, organization_name, contact_email, daily_request_limit, request_count, description, created_at, expires_at, last_used_at, active) 
VALUES 
('health_test_token_123', 'Test Health Institution', 'test@health.org', 1000, 0, 'API token for Test Health Institution', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, TRUE),
('health_regional_456', 'Regional Medical Center', 'contact@regional.med', 1000, 0, 'API token for Regional Medical Center', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, TRUE),
('health_research_789', 'University Research Lab', 'research@university.edu', 1000, 0, 'API token for University Research Lab', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), NULL, TRUE);