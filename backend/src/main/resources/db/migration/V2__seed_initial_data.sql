-- Seed mesas
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 1', 2, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 2', 2, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 3', 4, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 4', 4, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 5', 6, true);
INSERT INTO mesa (numero, capacidade, ativo) VALUES ('Mesa 6', 8, true);

-- Seed staff users
-- Password is 'admin123', hashed with BCrypt
INSERT INTO usuario (nome, email, senha_hash, role) VALUES ('Admin', 'admin@mesalive.com', '$2a$10$xx.IGsVRsJdoCisS3a8E5uqeeND0Fb9oeGiICn7f4VkVHUdz3QQXO', 'GERENTE');
INSERT INTO usuario (nome, email, senha_hash, role) VALUES ('Garçom João', 'joao@mesalive.com', '$2a$10$xx.IGsVRsJdoCisS3a8E5uqeeND0Fb9oeGiICn7f4VkVHUdz3QQXO', 'GARCOM');
