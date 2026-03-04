import { Component, OnInit, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, filter, catchError, tap, map } from 'rxjs/operators';

import { VendaService, VendaRequest } from '../services/venda.service';
import { ClienteService } from '../../clientes/services/cliente.service';
import { AppFormatter, BrMaskPipe } from '../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-vendas',
  standalone: true,
  imports: [CommonModule, FormsModule, BrMaskPipe],
  templateUrl: './vendas.html',
  styleUrls: ['./vendas.scss']
})
export class VendasComponent implements OnInit {
  private http = inject(HttpClient);
  private vendaService = inject(VendaService);
  private clienteService = inject(ClienteService);
  private destroyRef = inject(DestroyRef);

  loading = signal(false);
  showSuccessModal = signal(false);
  ultimoIdVenda: number | null = null;

  cliente = { nome: '', documento: '', telefone: '' };
  carrinho = signal<any[]>([]);

  formaPagamento = 'DINHEIRO';
  statusPagamento = 'PAGO';
  dataPrevisao: string = '';

  percentualDesconto = signal<number | null>(null);

  produtosDisponiveis = signal<any[]>([]);
  produtoSelecionado: any = null;

  quantidadeSelecionada: number | null = null;
  termoBuscaProduto = '';

  // --- ESTADOS DO AUTOCOMPLETE DE CLIENTES ---
  termoBuscaCliente = new Subject<string>();
  sugestoesClientes = signal<any[]>([]);
  buscandoClientes = signal(false);
  exibirSugestoes = signal(false);

  // Cache inteligente para evitar consultas desnecessárias no banco
  ultimoTermoSemResultado = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarProdutos();
    this.configurarBuscaClientesAutocomplete();
  }

  carregarProdutos(): void {
    this.http.get<any>('http://localhost:8080/api/produtos').subscribe({
      next: (res: any) => this.produtosDisponiveis.set(res.data ? res.data : res),
      error: () => console.error("Falha ao buscar produtos.")
    });
  }

  // ==========================================
  // LÓGICA DE AUTOCOMPLETE (RXJS AVANÇADO)
  // ==========================================
  private configurarBuscaClientesAutocomplete(): void {
    this.termoBuscaCliente.pipe(
      debounceTime(500), // Aguarda meio segundo após a digitação parar
      map(termo => termo.trim().toLowerCase()), // Força Case Insensitive e remove espaços extras
      distinctUntilChanged(), // Só dispara se a palavra realmente mudou
      filter(termo => {
        // Bloqueia buscas curtas demais
        if (termo.length < 3) {
          this.sugestoesClientes.set([]);
          this.exibirSugestoes.set(false);
          this.ultimoTermoSemResultado.set(null); // Reseta a memória de falhas
          this.buscandoClientes.set(false);
          return false;
        }

        // INTELIGÊNCIA: Se "car" não retornou nada antes, "carlos" também não vai retornar.
        // Bloqueia a requisição no front-end para salvar processamento do backend.
        const termoMorto = this.ultimoTermoSemResultado();
        if (termoMorto && termo.startsWith(termoMorto)) {
          this.sugestoesClientes.set([]);
          this.exibirSugestoes.set(false);
          this.buscandoClientes.set(false);
          return false;
        }

        return true;
      }),
      tap(() => {
        // Só exibe "buscando..." se realmente passou pelos filtros e vai chamar a API
        this.buscandoClientes.set(true);
        this.exibirSugestoes.set(true);
      }),
      switchMap(termo => {
        // switchMap cancela automaticamente a busca anterior se o usuário voltar a digitar
        return this.clienteService.buscarPorTermo(termo).pipe(
          map(res => ({ termo, clientes: res.data || [] })),
          catchError(() => of({ termo, clientes: [] })) // Fallback silencioso
        );
      }),
      takeUntilDestroyed(this.destroyRef) // Evita memory leak ao sair da tela
    ).subscribe({
      next: ({ termo, clientes }) => {
        this.buscandoClientes.set(false);
        this.sugestoesClientes.set(clientes);

        if (clientes.length === 0) {
          // Oculta sugestões e salva o termo na memória para bloquear as próximas letras
          this.exibirSugestoes.set(false);
          this.ultimoTermoSemResultado.set(termo);
        } else {
          // Exibe os clientes e limpa a memória de falhas
          this.exibirSugestoes.set(true);
          this.ultimoTermoSemResultado.set(null);
        }
      }
    });
  }

  onBuscaClienteInput(termo: string): void {
    this.cliente.nome = termo;
    this.termoBuscaCliente.next(termo);
  }

  selecionarClienteSugestao(clienteSelecionado: any): void {
    this.cliente.nome = clienteSelecionado.nome;
    this.cliente.documento = AppFormatter.documento(clienteSelecionado.documento);
    this.cliente.telefone = AppFormatter.telefone(clienteSelecionado.telefone);

    this.sugestoesClientes.set([]);
    this.exibirSugestoes.set(false);
  }

  esconderSugestoes(): void {
    setTimeout(() => this.exibirSugestoes.set(false), 200);
  }
  // ==========================================

  formatarTelefone(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, "");
    if (v.length > 11) v = v.slice(0, 11);
    if (v.length > 10) v = v.replace(/^(\d\d)(\d{5})(\d{4}).*/, "($1) $2-$3");
    else v = v.replace(/^(\d\d)(\d{4})(\d{0,4}).*/, "($1) $2-$3");
    this.cliente.telefone = v;
    input.value = v;
  }

  formatarDocumento(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, "");
    if (v.length > 14) v = v.slice(0, 14);
    if (v.length <= 11) {
      v = v.replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    } else {
      v = v.replace(/^(\d{2})(\d)/, "$1.$2").replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3").replace(/\.(\d{3})(\d)/, ".$1/$2").replace(/(\d{4})(\d)/, "$1-$2");
    }
    this.cliente.documento = v;
    input.value = v;
  }

  produtosFiltrados = computed(() => {
    const termo = this.termoBuscaProduto.toLowerCase();
    return this.produtosDisponiveis().filter(p => p.nome.toLowerCase().includes(termo));
  });

  selecionarProduto(produto: any): void {
    this.produtoSelecionado = produto;
    this.quantidadeSelecionada = null;
  }

  adicionarAoCarrinho(): void {
    if (!this.produtoSelecionado) return;

    const qtd = this.quantidadeSelecionada || 1;

    if (qtd > this.produtoSelecionado.estoqueAtual) {
      alert(`Estoque insuficiente! Disponível: ${this.produtoSelecionado.estoqueAtual}`);
      return;
    }

    const itemExistente = this.carrinho().find(i => i.id === this.produtoSelecionado.id);
    if (itemExistente) {
      this.carrinho.update(itens => itens.map(i =>
        i.id === this.produtoSelecionado.id
          ? { ...i, quantidade: i.quantidade + qtd, subtotal: (i.quantidade + qtd) * i.precoVenda }
          : i
      ));
    } else {
      this.carrinho.update(itens => [
        ...itens,
        { ...this.produtoSelecionado, quantidade: qtd, subtotal: this.produtoSelecionado.precoVenda * qtd }
      ]);
    }
    this.produtoSelecionado = null;
    this.quantidadeSelecionada = null;
    this.termoBuscaProduto = '';
  }

  removerDoCarrinho(index: number): void {
    this.carrinho.update(itens => itens.filter((_, i) => i !== index));
  }

  atualizarDesconto(valor: number | null): void {
    if (valor !== null && valor > 100) valor = 100;
    if (valor !== null && valor < 0) valor = 0;
    this.percentualDesconto.set(valor);
  }

  valorBruto = computed(() => this.carrinho().reduce((acc, item) => acc + item.subtotal, 0));

  valorDesconto = computed(() => {
    const perc = this.percentualDesconto() || 0;
    return (this.valorBruto() * perc) / 100;
  });

  valorLiquidoFinal = computed(() => this.valorBruto() - this.valorDesconto());

  finalizarVenda(): void {
    if (!this.cliente.nome || this.carrinho().length === 0) {
      alert('Informe o Cliente e adicione produtos ao carrinho.');
      return;
    }
    this.loading.set(true);

    const payload: VendaRequest = {
      cliente: {
        nome: this.cliente.nome,
        documento: this.cliente.documento.replace(/\D/g, ''),
        telefone: this.cliente.telefone.replace(/\D/g, '')
      },
      itens: this.carrinho().map(i => ({ produtoId: i.id, quantidade: i.quantidade, precoUnitario: i.precoVenda })),
      formaPagamento: this.formaPagamento,
      status: this.statusPagamento,
      dataPrevisao: this.dataPrevisao || undefined,
      percentualDesconto: this.percentualDesconto() || 0
    };

    this.vendaService.realizarVenda(payload).subscribe({
      next: (res: any) => {
        this.ultimoIdVenda = res.data.id;
        this.loading.set(false);
        this.showSuccessModal.set(true);
        this.carregarProdutos();
      },
      error: () => {
        this.loading.set(false);
        alert('Erro ao processar venda.');
      }
    });
  }

  baixarNotaFiscal(): void {
    if (!this.ultimoIdVenda) return;
    this.vendaService.downloadComprovante(this.ultimoIdVenda).subscribe({
      next: (blob: Blob) => {
        const fileURL = URL.createObjectURL(blob);
        window.open(fileURL, '_blank');
      }
    });
  }

  novaVenda(): void {
    this.carrinho.set([]);
    this.cliente = { nome: '', documento: '', telefone: '' };
    this.statusPagamento = 'PAGO';
    this.formaPagamento = 'DINHEIRO';
    this.percentualDesconto.set(null);
    this.quantidadeSelecionada = null;
    this.ultimoTermoSemResultado.set(null); // Reseta a memória de buscas ao criar nova venda
    this.showSuccessModal.set(false);
    this.ultimoIdVenda = null;
  }
}
