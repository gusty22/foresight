package br.com.foresight.modules.comercial.venda.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record VendaDto(
        Long id,
        String cliente,
        String documentoCliente,
        BigDecimal valorBruto,
        BigDecimal percentualDesconto,
        BigDecimal valorDesconto,
        BigDecimal valorTotal,
        LocalDateTime data,
        String formaPagamento,
        String statusPagamento,
        LocalDate dataPagamento,
        List<ItemVendaDto> itens
) {}