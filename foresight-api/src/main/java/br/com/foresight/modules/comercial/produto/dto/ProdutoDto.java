package br.com.foresight.modules.comercial.produto.dto;

import java.math.BigDecimal;

public record ProdutoDto(
        Long id,
        String nome,
        String codigoBarras,
        String imagemUrl,
        Long categoriaId,
        String categoriaNome,
        Long fornecedorId,
        String fornecedorNome,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Integer estoqueAtual,
        Integer estoqueMinimo,
        BigDecimal margemReal,
        BigDecimal lucroReal,
        String alertaStatus,
        // NOVOS CAMPOS PARA FINANCIAMENTO
        Long investidorId,
        String investidorNome,
        BigDecimal percentualLucroInvestidor
) {}