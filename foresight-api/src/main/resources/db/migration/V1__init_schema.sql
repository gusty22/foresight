-- 1. Tabela de usuários do sistema
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nome VARCHAR(150) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          senha VARCHAR(255) NOT NULL,
                          telefone VARCHAR(20),
                          role VARCHAR(50) NOT NULL DEFAULT 'ROLE_TENANT_ADMIN',
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);

-- 2. Empresas cadastradas no sistema
CREATE TABLE empresas (
                          id BIGSERIAL PRIMARY KEY,
                          usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                          nome VARCHAR(150) NOT NULL,
                          cnpj VARCHAR(20) NOT NULL UNIQUE,
                          email VARCHAR(255),
                          telefone VARCHAR(20),
                          cep VARCHAR(10),
                          logradouro VARCHAR(255),
                          numero VARCHAR(20),
                          bairro VARCHAR(100),
                          cidade VARCHAR(100),
                          estado VARCHAR(2),
                          tipo VARCHAR(50) NOT NULL,
                          prolabore_desejado NUMERIC(12,2) DEFAULT 0,
                          status VARCHAR(20) NOT NULL DEFAULT 'ATIVA',
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);

-- 3. Clientes (Sincronizado com Cliente.java)
CREATE TABLE clientes (
                          id BIGSERIAL PRIMARY KEY,
                          empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                          nome VARCHAR(150) NOT NULL,
                          documento VARCHAR(20),
                          telefone VARCHAR(20),
                          telefone_alternativo VARCHAR(20),
                          email VARCHAR(255),
                          data_nascimento_fundacao DATE,
                          cep VARCHAR(10),
                          logradouro VARCHAR(255),
                          numero VARCHAR(20),
                          bairro VARCHAR(100),
                          cidade VARCHAR(100),
                          estado VARCHAR(2),
                          tipo_cliente VARCHAR(10),
                          inscricao_estadual VARCHAR(50),
                          condicoes_especiais TEXT,
                          observacoes TEXT,
                          status_cliente VARCHAR(20),

    -- Campos da BaseTenantEntity
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);

-- 4. Categorias de Produto
CREATE TABLE categorias_produto (
                                    id BIGSERIAL PRIMARY KEY,
                                    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                                    nome VARCHAR(100) NOT NULL,
                                    cor_hexadecimal VARCHAR(7) DEFAULT '#CCCCCC',
                                    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    atualizado_em TIMESTAMP,
                                    criado_por VARCHAR(150),
                                    atualizado_por VARCHAR(150),
                                    version BIGINT DEFAULT 0
);

-- 5. Fornecedores
CREATE TABLE fornecedores (
                              id BIGSERIAL PRIMARY KEY,
                              empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                              nome VARCHAR(150) NOT NULL,
                              telefone VARCHAR(20),
                              cnpj VARCHAR(20),
                              criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              atualizado_em TIMESTAMP,
                              criado_por VARCHAR(150),
                              atualizado_por VARCHAR(150),
                              version BIGINT DEFAULT 0
);

-- 6. Produtos
CREATE TABLE produtos (
                          id BIGSERIAL PRIMARY KEY,
                          empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                          categoria_id BIGINT REFERENCES categorias_produto(id) ON DELETE SET NULL,
                          fornecedor_id BIGINT REFERENCES fornecedores(id) ON DELETE SET NULL,
                          nome VARCHAR(150) NOT NULL,
                          codigo_barras VARCHAR(50),
                          imagem_url VARCHAR(500),
                          preco_custo NUMERIC(12,2) NOT NULL,
                          preco_venda NUMERIC(12,2) NOT NULL,
                          estoque_atual INTEGER DEFAULT 0,
                          estoque_minimo INTEGER DEFAULT 5,
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0,
                          CONSTRAINT uk_produto_empresa_barcode UNIQUE (empresa_id, codigo_barras)
);

-- 7. Investidores
CREATE TABLE investidores (
                              id BIGSERIAL PRIMARY KEY,
                              empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                              nome VARCHAR(150) NOT NULL,
                              telefone VARCHAR(20),
                              chave_pix VARCHAR(150),
                              status VARCHAR(20) DEFAULT 'ATIVO',
                              criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              atualizado_em TIMESTAMP,
                              criado_por VARCHAR(150),
                              atualizado_por VARCHAR(150),
                              version BIGINT DEFAULT 0
);

-- 8. Lotes de Estoque
CREATE TABLE lotes_estoque (
                               id BIGSERIAL PRIMARY KEY,
                               empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                               produto_id BIGINT NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
                               investidor_id BIGINT REFERENCES investidores(id) ON DELETE SET NULL,
                               quantidade_inicial INTEGER NOT NULL,
                               quantidade_disponivel INTEGER NOT NULL,
                               custo_unitario NUMERIC(15,2) NOT NULL,
                               percentual_lucro_investidor NUMERIC(5,2) DEFAULT 0.00,
                               data_entrada TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               status VARCHAR(20) DEFAULT 'ABERTO',
                               criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               atualizado_em TIMESTAMP,
                               criado_por VARCHAR(150),
                               atualizado_por VARCHAR(150),
                               version BIGINT DEFAULT 0
);

-- 9. Vendas
CREATE TABLE vendas (
                        id BIGSERIAL PRIMARY KEY,
                        empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                        cliente_id BIGINT REFERENCES clientes(id) ON DELETE SET NULL,
                        cliente_nome_historico VARCHAR(150) NOT NULL,
                        documento_cliente VARCHAR(20),
                        telefone_cliente VARCHAR(20),
                        valor_bruto NUMERIC(15,2) NOT NULL DEFAULT 0.00,
                        percentual_desconto NUMERIC(5,2),
                        valor_desconto NUMERIC(15,2),
                        valor_total NUMERIC(15,2) NOT NULL,
                        forma_pagamento VARCHAR(50),
                        status_pagamento VARCHAR(20),
                        data_previsao_pagamento DATE,
                        data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        atualizado_em TIMESTAMP,
                        criado_por VARCHAR(150),
                        atualizado_por VARCHAR(150),
                        version BIGINT DEFAULT 0
);

-- 10. Itens de Venda
CREATE TABLE itens_venda (
                             id BIGSERIAL PRIMARY KEY,
                             venda_id BIGINT NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
                             produto_id BIGINT REFERENCES produtos(id) ON DELETE SET NULL,
                             lote_id BIGINT REFERENCES lotes_estoque(id) ON DELETE SET NULL,
                             quantidade INTEGER NOT NULL,
                             preco_unitario NUMERIC(15,2) NOT NULL,
                             custo_unitario_lote NUMERIC(15,2) NOT NULL
);

-- 11. Repasses Investidor
CREATE TABLE repasses_investidor (
                                     id BIGSERIAL PRIMARY KEY,
                                     empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                                     investidor_id BIGINT NOT NULL REFERENCES investidores(id) ON DELETE CASCADE,
                                     venda_id BIGINT NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
                                     item_venda_id BIGINT NOT NULL REFERENCES itens_venda(id) ON DELETE CASCADE,
                                     valor_lucro_total NUMERIC(15,2) NOT NULL,
                                     valor_repasse NUMERIC(15,2) NOT NULL,
                                     status VARCHAR(20) DEFAULT 'PENDENTE',
                                     data_pagamento TIMESTAMP,
                                     criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     atualizado_em TIMESTAMP,
                                     criado_por VARCHAR(150),
                                     atualizado_por VARCHAR(150),
                                     version BIGINT DEFAULT 0
);

-- 12. Despesas
CREATE TABLE despesas (
                          id BIGSERIAL PRIMARY KEY,
                          empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                          descricao VARCHAR(255) NOT NULL,
                          categoria VARCHAR(100),
                          valor NUMERIC(15,2) NOT NULL,
                          tipo VARCHAR(50) NOT NULL,
                          data_vencimento DATE,
                          data TIMESTAMP NOT NULL,
                          status VARCHAR(20) DEFAULT 'PAGO',
                          eh_pessoal BOOLEAN DEFAULT FALSE,
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);

-- 13. Fluxo de Caixa
CREATE TABLE fluxo_caixa (
                             id BIGSERIAL PRIMARY KEY,
                             empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                             descricao VARCHAR(255) NOT NULL,
                             valor NUMERIC(15,2) NOT NULL,
                             tipo VARCHAR(20) NOT NULL,
                             data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             saldo_apos_movimentacao NUMERIC(15,2) NOT NULL,
                             categoria_fluxo VARCHAR(50) DEFAULT 'EMPRESA',
                             estornado BOOLEAN DEFAULT FALSE,
                             referencia_estorno_id BIGINT REFERENCES fluxo_caixa(id),
                             origem VARCHAR(50),
                             origem_id BIGINT,
                             criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             atualizado_em TIMESTAMP,
                             criado_por VARCHAR(150),
                             atualizado_por VARCHAR(150),
                             version BIGINT DEFAULT 0
);

-- 14. Auditoria
CREATE TABLE logs_auditoria (
                                id BIGSERIAL PRIMARY KEY,
                                empresa_id BIGINT NOT NULL,
                                entidade_nome VARCHAR(150) NOT NULL,
                                entidade_id BIGINT,
                                acao VARCHAR(50) NOT NULL,
                                detalhes TEXT,
                                usuario_email VARCHAR(150) NOT NULL,
                                data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indices
CREATE INDEX idx_empresa_usuario ON empresas(usuario_id);
CREATE INDEX idx_cliente_empresa ON clientes(empresa_id);
CREATE INDEX idx_produto_empresa ON produtos(empresa_id);
CREATE INDEX idx_lote_produto ON lotes_estoque(produto_id);
CREATE INDEX idx_repasse_investidor ON repasses_investidor(investidor_id);