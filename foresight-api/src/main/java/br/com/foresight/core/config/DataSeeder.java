package br.com.foresight.core.config;

import br.com.foresight.modules.comercial.cliente.entity.Cliente;
import br.com.foresight.modules.comercial.cliente.repository.IClienteRepository;
import br.com.foresight.modules.comercial.investimento.entity.Investidor;
import br.com.foresight.modules.comercial.investimento.entity.LoteEstoque;
import br.com.foresight.modules.comercial.investimento.repository.IInvestidorRepository;
import br.com.foresight.modules.comercial.investimento.repository.ILoteEstoqueRepository;
import br.com.foresight.modules.comercial.produto.entity.Produto;
import br.com.foresight.modules.comercial.produto.repository.IProdutoRepository;
import br.com.foresight.modules.identity.empresa.entity.Empresa;
import br.com.foresight.modules.identity.empresa.enums.StatusEmpresa;
import br.com.foresight.modules.identity.empresa.model.TipoEmpresa;
import br.com.foresight.modules.identity.empresa.repository.IEmpresaRepository;
import br.com.foresight.modules.identity.usuario.entity.Usuario;
import br.com.foresight.modules.identity.usuario.enums.Role;
import br.com.foresight.modules.identity.usuario.repository.IUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final IEmpresaRepository empresaRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IInvestidorRepository investidorRepository;
    private final IProdutoRepository produtoRepository;
    private final IClienteRepository clienteRepository;
    private final ILoteEstoqueRepository loteEstoqueRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // 1. VERIFICAÇÃO DE SEGURANÇA (Garante que só vai rodar uma vez)
        if (usuarioRepository.findByEmail("projeto@foresight.com").isPresent()) {
            System.out.println("✅ [FORESIGHT SEEDER] O Banco de Dados já está semeado e pronto para a apresentação.");
            return;
        }

        System.out.println("⏳ [FORESIGHT SEEDER] Construindo ambiente histórico de 1 mês de uso...");

        // ==========================================
        // 2. CRIAÇÃO DO USUÁRIO (PRIMEIRO PARA GERAR ID)
        // ==========================================
        Usuario prof = new Usuario();
        prof.setNome("Avaliador Foresight");
        prof.setEmail("projeto@foresight.com");
        prof.setSenha(passwordEncoder.encode("admin123"));
        prof.setRole(Role.ROLE_TENANT_ADMIN); // Direciona direto para o painel comercial
        prof.setTelefone("(34) 99999-0000");
        prof = usuarioRepository.save(prof);

        // ==========================================
        // 3. CRIAÇÃO DA EMPRESA BASE
        // ==========================================
        Empresa empresa = new Empresa();
        empresa.setNome("Foresight Oficial (Apresentação)");
        empresa.setCnpj("12.345.678/0001-99");
        empresa.setEmail("contato@foresight.com.br");
        empresa.setStatus(StatusEmpresa.ATIVA);
        empresa.setTipo(TipoEmpresa.values()[0]); // Pega o primeiro tipo de empresa cadastrado no Enum dinamicamente

        // CORREÇÃO CRÍTICA APLICADA AQUI: Vinculando ao nome exato do atributo da entidade
        empresa.setDono(prof);

        empresa = empresaRepository.save(empresa);

        // ==========================================
        // 4. CRIAÇÃO DA CARTEIRA DE CLIENTES
        // ==========================================
        Cliente cliente1 = new Cliente();
        cliente1.setNome("Tech Solutions Informática LTDA");
        cliente1.setEmail("compras@techsolutions.com.br");
        cliente1.setTelefone("(11) 99999-1234");
        cliente1.setDocumento("45.678.901/0001-23");
        cliente1.setStatusCliente("ATIVO");
        cliente1.setEmpresa(empresa);
        clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNome("Carlos Mendes Arquitetura");
        cliente2.setEmail("carlos.mendes@email.com");
        cliente2.setTelefone("(34) 98888-5678");
        cliente2.setDocumento("123.456.789-00");
        cliente2.setStatusCliente("ATIVO");
        cliente2.setEmpresa(empresa);
        clienteRepository.save(cliente2);

        Cliente cliente3 = new Cliente();
        cliente3.setNome("Supermercado Central");
        cliente3.setEmail("financeiro@central.com");
        cliente3.setTelefone("(31) 97777-4321");
        cliente3.setEmpresa(empresa);
        clienteRepository.save(cliente3);

        // ==========================================
        // 5. CRIAÇÃO DE PRODUTOS NO CATÁLOGO
        // ==========================================
        Produto prod1 = new Produto();
        prod1.setNome("Notebook Dell XPS 13");
        prod1.setPrecoCusto(new BigDecimal("6000.00")); // Atributo obrigatório adicionado
        prod1.setPrecoVenda(new BigDecimal("8500.00"));
        prod1.setEstoqueAtual(3); // Atualizado para refletir o Lote
        prod1.setEmpresa(empresa);
        prod1 = produtoRepository.save(prod1);

        Produto prod2 = new Produto();
        prod2.setNome("Smartphone Galaxy S23 Ultra");
        prod2.setPrecoCusto(new BigDecimal("4500.00"));
        prod2.setPrecoVenda(new BigDecimal("6200.00"));
        prod2.setEstoqueAtual(5);
        prod2.setEmpresa(empresa);
        prod2 = produtoRepository.save(prod2);

        Produto prod3 = new Produto();
        prod3.setNome("Monitor LG Ultrawide 29\"");
        prod3.setPrecoCusto(new BigDecimal("800.00"));
        prod3.setPrecoVenda(new BigDecimal("1200.00"));
        prod3.setEstoqueAtual(15);
        prod3.setEmpresa(empresa);
        prod3 = produtoRepository.save(prod3);

        Produto prod4 = new Produto();
        prod4.setNome("Teclado Mecânico Keychron K2");
        prod4.setPrecoCusto(new BigDecimal("250.00"));
        prod4.setPrecoVenda(new BigDecimal("450.00"));
        prod4.setEstoqueAtual(0); // Produto sem estoque
        prod4.setEstoqueMinimo(5);
        prod4.setEmpresa(empresa);
        produtoRepository.save(prod4);

        // ==========================================
        // 6. CRIAÇÃO DOS SÓCIOS/INVESTIDORES
        // ==========================================
        Investidor inv1 = new Investidor();
        inv1.setNome("Dr. Renato (Investidor Master)");
        inv1.setTelefone("(34) 99999-1111");
        inv1.setChavePix("renato@banco.com.br");
        inv1.setStatus("ATIVO");
        inv1.setEmpresa(empresa);
        inv1 = investidorRepository.save(inv1);

        Investidor inv2 = new Investidor();
        inv2.setNome("Fundo Alpha Capital");
        inv2.setTelefone("(11) 98888-2222");
        inv2.setChavePix("alpha@pix.com.br");
        inv2.setStatus("ATIVO");
        inv2.setEmpresa(empresa);
        inv2 = investidorRepository.save(inv2);

        // ==========================================
        // 7. CRIAÇÃO DOS LOTES FINANCIADOS (HISTÓRICO)
        // ==========================================

        // Lote de 45 dias (Quase Esgotado) - Simula vendas ativas
        LoteEstoque lote1 = new LoteEstoque();
        lote1.setProduto(prod1);
        lote1.setInvestidor(inv1);
        lote1.setQuantidadeInicial(10);
        lote1.setQuantidadeDisponivel(3);
        lote1.setCustoUnitario(new BigDecimal("6000.00"));
        lote1.setPercentualLucroInvestidor(new BigDecimal("50.00"));
        lote1.setStatus("ABERTO");
        lote1.setDataEntrada(LocalDateTime.now().minusDays(45));
        lote1.setEmpresa(empresa);
        loteEstoqueRepository.save(lote1);

        // Lote de 30 dias (Giro Constante)
        LoteEstoque lote2 = new LoteEstoque();
        lote2.setProduto(prod2);
        lote2.setInvestidor(inv2);
        lote2.setQuantidadeInicial(20);
        lote2.setQuantidadeDisponivel(5);
        lote2.setCustoUnitario(new BigDecimal("4500.00"));
        lote2.setPercentualLucroInvestidor(new BigDecimal("40.00"));
        lote2.setStatus("ABERTO");
        lote2.setDataEntrada(LocalDateTime.now().minusDays(30));
        lote2.setEmpresa(empresa);
        loteEstoqueRepository.save(lote2);

        // Lote de 5 dias (Recém Adicionado)
        LoteEstoque lote3 = new LoteEstoque();
        lote3.setProduto(prod3);
        lote3.setInvestidor(inv1);
        lote3.setQuantidadeInicial(15);
        lote3.setQuantidadeDisponivel(15);
        lote3.setCustoUnitario(new BigDecimal("800.00"));
        lote3.setPercentualLucroInvestidor(new BigDecimal("60.00"));
        lote3.setStatus("ABERTO");
        lote3.setDataEntrada(LocalDateTime.now().minusDays(5));
        lote3.setEmpresa(empresa);
        loteEstoqueRepository.save(lote3);

        System.out.println("✅ [FORESIGHT SEEDER] Ambiente de Apresentação gerado com sucesso! Dados consolidados.");
    }
}