package br.com.foresight.modules.comercial.apoio.service;

import br.com.foresight.core.tenant.TenantContext;
import br.com.foresight.modules.comercial.apoio.dto.CategoriaProdutoDto;
import br.com.foresight.modules.comercial.apoio.dto.FornecedorDto;
import br.com.foresight.modules.comercial.apoio.repository.ICategoriaProdutoRepository;
import br.com.foresight.modules.comercial.apoio.repository.IFornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApoioService {
    private final ICategoriaProdutoRepository categoriaRepository;
    private final IFornecedorRepository fornecedorRepository;

    public List<CategoriaProdutoDto> listarCategorias() {
        return categoriaRepository.findAllByEmpresaId(TenantContext.getCurrentTenant()).stream()
                .map(c -> new CategoriaProdutoDto(c.getId(), c.getNome(), c.getCorHexadecimal()))
                .toList();
    }

    public List<FornecedorDto> listarFornecedores() {
        return fornecedorRepository.findAllByEmpresaId(TenantContext.getCurrentTenant()).stream()
                .map(f -> new FornecedorDto(f.getId(), f.getNome(), f.getTelefone(), f.getCnpj()))
                .toList();
    }
}