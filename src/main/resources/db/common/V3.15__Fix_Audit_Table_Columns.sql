-- Migration V3.15 - Fix Audit Table Column Names
--
-- OBJETIVO: Corrigir nomes de colunas _mod nas tabelas de auditoria
-- para corresponder aos nomes das propriedades das entidades (não dos nomes das colunas FK)
--
-- PROBLEMA: Hibernate Envers usa os nomes das propriedades Java para gerar
-- os nomes das colunas *_mod, não os nomes das colunas do banco.
-- Exemplo: propriedade "emprestimo" gera "emprestimo_mod", não "emprestimo_id_mod"

-- =====================================================
-- EMPRESTIMO_DEVOLUCAO_ITEM_AUD
-- =====================================================

-- Adicionar coluna status_devolucao_mod se não existir
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'emprestimo_devolucao_item_aud' 
        AND column_name = 'status_devolucao_mod'
    ) THEN
        -- Se status_mod existe, renomear
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'emprestimo_devolucao_item_aud' 
            AND column_name = 'status_mod'
        ) THEN
            ALTER TABLE emprestimo_devolucao_item_aud 
                RENAME COLUMN status_mod TO status_devolucao_mod;
        ELSE
            -- Caso contrário, adicionar a coluna
            ALTER TABLE emprestimo_devolucao_item_aud 
                ADD COLUMN status_devolucao_mod BOOLEAN;
        END IF;
    END IF;
END $$;

-- Adicionar/renomear item_mod
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'emprestimo_devolucao_item_aud' 
        AND column_name = 'item_mod'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'emprestimo_devolucao_item_aud' 
            AND column_name = 'item_id_mod'
        ) THEN
            ALTER TABLE emprestimo_devolucao_item_aud 
                RENAME COLUMN item_id_mod TO item_mod;
        ELSE
            ALTER TABLE emprestimo_devolucao_item_aud 
                ADD COLUMN item_mod BOOLEAN;
        END IF;
    END IF;
END $$;

-- Adicionar/renomear emprestimo_mod
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'emprestimo_devolucao_item_aud' 
        AND column_name = 'emprestimo_mod'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'emprestimo_devolucao_item_aud' 
            AND column_name = 'emprestimo_id_mod'
        ) THEN
            ALTER TABLE emprestimo_devolucao_item_aud 
                RENAME COLUMN emprestimo_id_mod TO emprestimo_mod;
        ELSE
            ALTER TABLE emprestimo_devolucao_item_aud 
                ADD COLUMN emprestimo_mod BOOLEAN;
        END IF;
    END IF;
END $$;

-- =====================================================
-- EMPRESTIMO_ITEM_AUD
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_item_aud' AND column_name = 'item_mod') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_item_aud' AND column_name = 'item_id_mod') THEN
            ALTER TABLE emprestimo_item_aud RENAME COLUMN item_id_mod TO item_mod;
        ELSE
            ALTER TABLE emprestimo_item_aud ADD COLUMN item_mod BOOLEAN;
        END IF;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_item_aud' AND column_name = 'emprestimo_mod') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_item_aud' AND column_name = 'emprestimo_id_mod') THEN
            ALTER TABLE emprestimo_item_aud RENAME COLUMN emprestimo_id_mod TO emprestimo_mod;
        ELSE
            ALTER TABLE emprestimo_item_aud ADD COLUMN emprestimo_mod BOOLEAN;
        END IF;
    END IF;
END $$;

-- =====================================================
-- EMPRESTIMO_AUD
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_aud' AND column_name = 'usuario_responsavel_mod') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_aud' AND column_name = 'usuario_responsavel_id_mod') THEN
            ALTER TABLE emprestimo_aud RENAME COLUMN usuario_responsavel_id_mod TO usuario_responsavel_mod;
        ELSE
            ALTER TABLE emprestimo_aud ADD COLUMN usuario_responsavel_mod BOOLEAN;
        END IF;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_aud' AND column_name = 'usuario_emprestimo_mod') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_aud' AND column_name = 'usuario_emprestimo_id_mod') THEN
            ALTER TABLE emprestimo_aud RENAME COLUMN usuario_emprestimo_id_mod TO usuario_emprestimo_mod;
        ELSE
            ALTER TABLE emprestimo_aud ADD COLUMN usuario_emprestimo_mod BOOLEAN;
        END IF;
    END IF;
END $$;

-- Add missing collection mod columns for Emprestimo
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_aud' AND column_name = 'emprestimo_item_mod') THEN
        ALTER TABLE emprestimo_aud ADD COLUMN emprestimo_item_mod BOOLEAN;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'emprestimo_aud' AND column_name = 'emprestimo_devolucao_item_mod') THEN
        ALTER TABLE emprestimo_aud ADD COLUMN emprestimo_devolucao_item_mod BOOLEAN;
    END IF;
END $$;

-- =====================================================
-- ITEM_AUD
-- =====================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'item_aud' AND column_name = 'grupo_mod') THEN
        IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'item_aud' AND column_name = 'grupo_id_mod') THEN
            ALTER TABLE item_aud RENAME COLUMN grupo_id_mod TO grupo_mod;
        ELSE
            ALTER TABLE item_aud ADD COLUMN grupo_mod BOOLEAN;
        END IF;
    END IF;
END $$;

-- Add missing collection mod column for Item
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'item_aud' AND column_name = 'image_item_mod') THEN
        ALTER TABLE item_aud ADD COLUMN image_item_mod BOOLEAN;
    END IF;
END $$;

-- =====================================================
-- SAIDA_ITEM_AUD, RESERVA_ITEM_AUD, COMPRA_ITEM_AUD, SOLICITACAO_ITEM_AUD, ITEM_IMAGE_AUD
-- =====================================================

-- SAIDA_ITEM_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_item_aud' AND column_name = 'item_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_item_aud' AND column_name = 'item_id_mod') THEN ALTER TABLE saida_item_aud RENAME COLUMN item_id_mod TO item_mod;
    ELSE ALTER TABLE saida_item_aud ADD COLUMN item_mod BOOLEAN; END IF; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_item_aud' AND column_name = 'saida_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_item_aud' AND column_name = 'saida_id_mod') THEN ALTER TABLE saida_item_aud RENAME COLUMN saida_id_mod TO saida_mod;
    ELSE ALTER TABLE saida_item_aud ADD COLUMN saida_mod BOOLEAN; END IF; END IF; END $$;

-- RESERVA_ITEM_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reserva_item_aud' AND column_name = 'item_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reserva_item_aud' AND column_name = 'item_id_mod') THEN ALTER TABLE reserva_item_aud RENAME COLUMN item_id_mod TO item_mod;
    ELSE ALTER TABLE reserva_item_aud ADD COLUMN item_mod BOOLEAN; END IF; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reserva_item_aud' AND column_name = 'reserva_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reserva_item_aud' AND column_name = 'reserva_id_mod') THEN ALTER TABLE reserva_item_aud RENAME COLUMN reserva_id_mod TO reserva_mod;
    ELSE ALTER TABLE reserva_item_aud ADD COLUMN reserva_mod BOOLEAN; END IF; END IF; END $$;

-- COMPRA_ITEM_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_item_aud' AND column_name = 'item_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_item_aud' AND column_name = 'item_id_mod') THEN ALTER TABLE compra_item_aud RENAME COLUMN item_id_mod TO item_mod;
    ELSE ALTER TABLE compra_item_aud ADD COLUMN item_mod BOOLEAN; END IF; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_item_aud' AND column_name = 'compra_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_item_aud' AND column_name = 'compra_id_mod') THEN ALTER TABLE compra_item_aud RENAME COLUMN compra_id_mod TO compra_mod;
    ELSE ALTER TABLE compra_item_aud ADD COLUMN compra_mod BOOLEAN; END IF; END IF; END $$;

-- SOLICITACAO_ITEM_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'solicitacao_item_aud' AND column_name = 'item_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'solicitacao_item_aud' AND column_name = 'item_id_mod') THEN ALTER TABLE solicitacao_item_aud RENAME COLUMN item_id_mod TO item_mod;
    ELSE ALTER TABLE solicitacao_item_aud ADD COLUMN item_mod BOOLEAN; END IF; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'solicitacao_item_aud' AND column_name = 'solicitacao_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'solicitacao_item_aud' AND column_name = 'solicitacao_id_mod') THEN ALTER TABLE solicitacao_item_aud RENAME COLUMN solicitacao_id_mod TO solicitacao_mod;
    ELSE ALTER TABLE solicitacao_item_aud ADD COLUMN solicitacao_mod BOOLEAN; END IF; END IF; END $$;

-- ITEM_IMAGE_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'item_image_aud' AND column_name = 'item_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'item_image_aud' AND column_name = 'item_id_mod') THEN ALTER TABLE item_image_aud RENAME COLUMN item_id_mod TO item_mod;
    ELSE ALTER TABLE item_image_aud ADD COLUMN item_mod BOOLEAN; END IF; END IF; END $$;

-- =====================================================
-- SAIDA_AUD, RESERVA_AUD, COMPRA_AUD, SOLICITACAO_AUD, NADA_CONSTA_AUD
-- =====================================================

-- SAIDA_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_aud' AND column_name = 'usuario_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_aud' AND column_name = 'usuario_id_mod') THEN ALTER TABLE saida_aud RENAME COLUMN usuario_id_mod TO usuario_mod;
    ELSE ALTER TABLE saida_aud ADD COLUMN usuario_mod BOOLEAN; END IF; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_aud' AND column_name = 'emprestimo_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'saida_aud' AND column_name = 'emprestimo_id_mod') THEN ALTER TABLE saida_aud RENAME COLUMN emprestimo_id_mod TO emprestimo_mod;
    ELSE ALTER TABLE saida_aud ADD COLUMN emprestimo_mod BOOLEAN; END IF; END IF; END $$;

-- RESERVA_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reserva_aud' AND column_name = 'usuario_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'reserva_aud' AND column_name = 'usuario_id_mod') THEN ALTER TABLE reserva_aud RENAME COLUMN usuario_id_mod TO usuario_mod;
    ELSE ALTER TABLE reserva_aud ADD COLUMN usuario_mod BOOLEAN; END IF; END IF; END $$;

-- COMPRA_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_aud' AND column_name = 'fornecedor_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_aud' AND column_name = 'fornecedor_id_mod') THEN ALTER TABLE compra_aud RENAME COLUMN fornecedor_id_mod TO fornecedor_mod;
    ELSE ALTER TABLE compra_aud ADD COLUMN fornecedor_mod BOOLEAN; END IF; END IF; END $$;
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_aud' AND column_name = 'usuario_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'compra_aud' AND column_name = 'usuario_id_mod') THEN ALTER TABLE compra_aud RENAME COLUMN usuario_id_mod TO usuario_mod;
    ELSE ALTER TABLE compra_aud ADD COLUMN usuario_mod BOOLEAN; END IF; END IF; END $$;

-- SOLICITACAO_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'solicitacao_aud' AND column_name = 'usuario_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'solicitacao_aud' AND column_name = 'usuario_id_mod') THEN ALTER TABLE solicitacao_aud RENAME COLUMN usuario_id_mod TO usuario_mod;
    ELSE ALTER TABLE solicitacao_aud ADD COLUMN usuario_mod BOOLEAN; END IF; END IF; END $$;

-- NADA_CONSTA_AUD
DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'nada_consta_aud' AND column_name = 'usuario_mod') THEN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'nada_consta_aud' AND column_name = 'usuario_id_mod') THEN ALTER TABLE nada_consta_aud RENAME COLUMN usuario_id_mod TO usuario_mod;
    ELSE ALTER TABLE nada_consta_aud ADD COLUMN usuario_mod BOOLEAN; END IF; END IF; END $$;
