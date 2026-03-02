-- =============================================================================
-- 1. USUÁRIOS (Identity Root)
-- =============================================================================
CREATE TABLE usuarios (
                          id BIGSERIAL PRIMARY KEY,
                          nome VARCHAR(150) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          senha VARCHAR(255) NOT NULL,
                          telefone VARCHAR(20),
                          role VARCHAR(50) NOT NULL DEFAULT 'ROLE_TENANT_ADMIN', -- NOVA: Controle de Acesso (RBAC)

    -- Colunas de Auditoria (BaseAuditEntity)
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);

-- =============================================================================
-- 2. EMPRESAS (Tenant Root)
-- =============================================================================
CREATE TABLE empresas (
                          id BIGSERIAL PRIMARY KEY,
                          usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
                          nome VARCHAR(150) NOT NULL,
                          cnpj VARCHAR(20) NOT NULL UNIQUE, -- ATUALIZADO: CNPJ deve ser único na plataforma
                          email VARCHAR(255),
                          telefone VARCHAR(20),
                          cep VARCHAR(10),
                          logradouro VARCHAR(255),
                          numero VARCHAR(20),
                          bairro VARCHAR(100),
                          cidade VARCHAR(100),
                          estado VARCHAR(2),
                          tipo VARCHAR(50) NOT NULL, -- SERVICO, PRODUTO, SERVICO_PRODUTO
                          prolabore_desejado NUMERIC(12,2) DEFAULT 0,
                          status VARCHAR(20) NOT NULL DEFAULT 'ATIVA', -- NOVA: Gerenciamento pelo Super Admin (Backoffice)

    -- Colunas de Auditoria
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);
CREATE INDEX idx_empresa_usuario ON empresas(usuario_id);

-- =============================================================================
-- 3. CLIENTES (Atualizado para CRM Cognitivo)
-- =============================================================================
CREATE TABLE clientes (
                          id BIGSERIAL PRIMARY KEY,
                          empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                          nome VARCHAR(150) NOT NULL,
                          nome_fantasia VARCHAR(150), -- NOVA: Específico para clientes PJ
                          documento VARCHAR(20),
                          tipo_cliente VARCHAR(10) DEFAULT 'PF',
                          telefone VARCHAR(20),
                          telefone_alternativo VARCHAR(20),
                          email VARCHAR(255),
                          cep VARCHAR(10),
                          logradouro VARCHAR(255),
                          numero VARCHAR(20),
                          bairro VARCHAR(100),
                          cidade VARCHAR(100),
                          estado VARCHAR(2),
                          inscricao_estadual VARCHAR(50),
                          observacoes TEXT,
                          condicoes_especiais TEXT,
                          data_nascimento_fundacao DATE, -- ATUALIZADA: Suporta PF e PJ
                          status_cliente VARCHAR(20) DEFAULT 'ATIVO',

    -- Colunas de Auditoria
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);
CREATE INDEX idx_cliente_empresa ON clientes(empresa_id);
CREATE INDEX idx_cliente_nome_doc ON clientes(nome, documento);

-- =============================================================================
-- 4. PRODUTOS (Catálogo)
-- =============================================================================
CREATE TABLE produtos (
                          id BIGSERIAL PRIMARY KEY,
                          empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                          nome VARCHAR(150) NOT NULL,
                          categoria VARCHAR(100),
                          preco_custo NUMERIC(12,2) NOT NULL,
                          preco_venda NUMERIC(12,2) NOT NULL,
                          estoque_atual INTEGER DEFAULT 0,
                          estoque_minimo INTEGER DEFAULT 5,

    -- Colunas de Auditoria
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);
CREATE INDEX idx_produto_empresa ON produtos(empresa_id);
CREATE INDEX idx_produto_categoria ON produtos(categoria);

-- =============================================================================
-- 5. VENDAS (Atualizado com Lógica de Descontos)
-- =============================================================================
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
                        status_pagamento VARCHAR(20), -- PENDENTE, PAGO, CANCELADO
                        data_previsao_pagamento DATE,
                        data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Colunas de Auditoria
                        criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        atualizado_em TIMESTAMP,
                        criado_por VARCHAR(150),
                        atualizado_por VARCHAR(150),
                        version BIGINT DEFAULT 0
);
CREATE INDEX idx_venda_empresa ON vendas(empresa_id);
CREATE INDEX idx_venda_data ON vendas(data);
CREATE INDEX idx_venda_status ON vendas(status_pagamento);

-- =============================================================================
-- 6. ITENS DE VENDA
-- =============================================================================
CREATE TABLE itens_venda (
                             id BIGSERIAL PRIMARY KEY,
                             venda_id BIGINT NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
                             produto_id BIGINT REFERENCES produtos(id) ON DELETE SET NULL,
                             quantidade INTEGER NOT NULL,
                             preco_unitario NUMERIC(15,2) NOT NULL
);
CREATE INDEX idx_item_venda ON itens_venda(venda_id);

-- =============================================================================
-- 7. DESPESAS
-- =============================================================================
CREATE TABLE despesas (
                          id BIGSERIAL PRIMARY KEY,
                          empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                          descricao VARCHAR(255) NOT NULL,
                          categoria VARCHAR(100),
                          valor NUMERIC(15,2) NOT NULL,
                          tipo VARCHAR(50) NOT NULL, -- FIXA, VARIAVEL
                          data_vencimento DATE,
                          data TIMESTAMP NOT NULL,
                          status VARCHAR(20) DEFAULT 'PAGO', -- PENDENTE, PAGO, ATRASADO
                          eh_pessoal BOOLEAN DEFAULT FALSE,

    -- Colunas de Auditoria
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          atualizado_em TIMESTAMP,
                          criado_por VARCHAR(150),
                          atualizado_por VARCHAR(150),
                          version BIGINT DEFAULT 0
);
CREATE INDEX idx_despesa_empresa ON despesas(empresa_id);
CREATE INDEX idx_despesa_status ON despesas(status);

-- =============================================================================
-- 8. FLUXO DE CAIXA
-- =============================================================================
CREATE TABLE fluxo_caixa (
                             id BIGSERIAL PRIMARY KEY,
                             empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
                             descricao VARCHAR(255) NOT NULL,
                             valor NUMERIC(15,2) NOT NULL,
                             tipo VARCHAR(20) NOT NULL, -- ENTRADA, SAIDA
                             data_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             saldo_apos_movimentacao NUMERIC(15,2) NOT NULL,
                             categoria_fluxo VARCHAR(50) DEFAULT 'EMPRESA', -- EMPRESA, PESSOAL

                             estornado BOOLEAN DEFAULT FALSE,
                             referencia_estorno_id BIGINT REFERENCES fluxo_caixa(id),
                             origem VARCHAR(50), -- VENDA, DESPESA, MANUAL
                             origem_id BIGINT,

    -- Colunas de Auditoria
                             criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             atualizado_em TIMESTAMP,
                             criado_por VARCHAR(150),
                             atualizado_por VARCHAR(150),
                             version BIGINT DEFAULT 0
);
CREATE INDEX idx_fluxo_empresa ON fluxo_caixa(empresa_id);
CREATE INDEX idx_fluxo_data ON fluxo_caixa(data_hora);

-- =============================================================================
-- 9. AUDITORIA
-- =============================================================================
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
CREATE INDEX idx_auditoria_empresa ON logs_auditoria(empresa_id);
CREATE INDEX idx_auditoria_data ON logs_auditoria(data_hora);