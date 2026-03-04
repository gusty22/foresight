import { Component, OnInit, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProdutoService } from '../services/produto.service';
import { ProdutoDto } from '../models/produto.dto';
import { BrMaskPipe, AppFormatter } from '../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-produtos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './produtos.html',
  styleUrls: ['./produtos.scss']
})
export class ProdutosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private produtoService = inject(ProdutoService);
  private destroyRef = inject(DestroyRef);

  // --- ESTADO REATIVO ---
  listaOriginal = signal<ProdutoDto[]>([]);
  loading = signal(false);
  salvando = signal(false);

  // --- PAGINAÇÃO ---
  paginaAtual = signal(1);
  itensPorPagina = signal(10);

  // --- CONTROLE DE MODAL E EDIÇÃO ---
  exibirModalEdicao = signal(false);
  produtoParaEdicaoId: number | null = null;
  exibicaoCusto = signal('');
  exibicaoVenda = signal('');

  // --- CONTROLE DE FILTROS AVANÇADOS ---
  exibirFiltrosAvancados = signal(false);
  filtrosAtivos = signal({ termo: '', categoria: '', status: 'TODOS', limiteEstoque: null as number | null });

  // 1. Formulário de Filtros (Seguro, sem ngModel)
  filtroForm: FormGroup = this.fb.group({
    termo: [''],
    categoria: [''],
    status: ['TODOS'], // TODOS, OK, BAIXO
    limiteEstoque: [null]
  });

  // 2. Formulário de Cadastro/Edição de Produto
  produtoForm: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    categoria: [''],
    precoCusto: [null, [Validators.required, Validators.min(0)]],
    precoVenda: [null, [Validators.required, Validators.min(0)]],
    estoqueAtual: [null, [Validators.required, Validators.min(0)]],
    estoqueMinimo: [5, [Validators.required, Validators.min(1)]] // Padrão 5
  });

  get f() { return this.produtoForm.controls; }

  ngOnInit(): void {
    this.listarProdutos();
    this.escutarMudancasDeFiltro();
  }

  // --- LÓGICA DE FILTRAGEM DE ALTA PERFORMANCE ---
  private escutarMudancasDeFiltro(): void {
    this.filtroForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(valores => {
        this.filtrosAtivos.set({
          termo: (valores.termo || '').toLowerCase(),
          categoria: (valores.categoria || '').toLowerCase(),
          status: valores.status || 'TODOS',
          limiteEstoque: valores.limiteEstoque
        });
        this.paginaAtual.set(1); // Reseta a paginação ao filtrar
      });
  }

  produtosFiltrados = computed(() => {
    const { termo, categoria, status, limiteEstoque } = this.filtrosAtivos();

    return this.listaOriginal().filter(p => {
      // Regra 1: Nome
      const matchNome = p.nome.toLowerCase().includes(termo);

      // Regra 2: Categoria
      const matchCat = p.categoria ? p.categoria.toLowerCase().includes(categoria) : true;

      // Regra 3: Status de Escassez (Estoque Crítico)
      const limite = p.estoqueMinimo || 5;
      const isCritico = p.estoqueAtual <= limite;
      const matchStatus = status === 'TODOS' || (status === 'BAIXO' && isCritico) || (status === 'OK' && !isCritico);

      // Regra 4: Limite de Quantidade
      const matchLimite = limiteEstoque === null || p.estoqueAtual <= limiteEstoque;

      return matchNome && matchCat && matchStatus && matchLimite;
    });
  });

  produtosPaginados = computed(() => {
    const inicio = (this.paginaAtual() - 1) * this.itensPorPagina();
    return this.produtosFiltrados().slice(inicio, inicio + this.itensPorPagina());
  });

  totalPaginas = computed(() => Math.ceil(this.produtosFiltrados().length / this.itensPorPagina()) || 1);

  limparFiltros(): void {
    this.filtroForm.reset({ status: 'TODOS' });
  }

  // --- INTEGRAÇÃO COM A API ---
  listarProdutos(): void {
    this.loading.set(true);
    this.produtoService.listar().subscribe({
      next: (res) => {
        this.listaOriginal.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao listar produtos', err);
        this.loading.set(false);
      }
    });
  }

  salvarProduto(): void {
    if (this.produtoForm.invalid) {
      this.produtoForm.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    const payload = this.produtoForm.value;

    // Fallback de segurança contra nulls maliciosos no backend
    payload.precoCusto = payload.precoCusto || 0;
    payload.precoVenda = payload.precoVenda || 0;
    payload.estoqueAtual = payload.estoqueAtual || 0;
    payload.estoqueMinimo = payload.estoqueMinimo || 5;

    if (this.produtoParaEdicaoId) {
      this.produtoService.atualizar(this.produtoParaEdicaoId, payload).subscribe({
        next: () => this.concluirSalvamento(),
        error: () => this.salvando.set(false)
      });
    } else {
      this.produtoService.criar(payload).subscribe({
        next: () => this.concluirSalvamento(),
        error: () => this.salvando.set(false)
      });
    }
  }

  excluirProduto(id: number): void {
    if (confirm('Atenção: Tem certeza que deseja excluir este produto?')) {
      this.loading.set(true);
      this.produtoService.excluir(id).subscribe({
        next: () => this.listarProdutos(),
        error: () => this.loading.set(false)
      });
    }
  }

  private concluirSalvamento(): void {
    this.fecharModal();
    this.salvando.set(false);
    this.listarProdutos();
  }

  // --- CONTROLE DE UI ---
  abrirNovoProduto(): void {
    this.produtoParaEdicaoId = null;
    this.produtoForm.reset({ estoqueMinimo: 5 });
    this.exibicaoCusto.set('');
    this.exibicaoVenda.set('');
    this.exibirModalEdicao.set(true);
  }

  abrirEdicao(produto: any): void {
    this.produtoParaEdicaoId = produto.id;
    this.produtoForm.patchValue({
      nome: produto.nome,
      categoria: produto.categoria,
      precoCusto: produto.precoCusto,
      precoVenda: produto.precoVenda,
      estoqueAtual: produto.estoqueAtual,
      estoqueMinimo: produto.estoqueMinimo || 5
    });

    this.exibicaoCusto.set(AppFormatter.decimal(produto.precoCusto));
    this.exibicaoVenda.set(AppFormatter.decimal(produto.precoVenda));

    this.exibirModalEdicao.set(true);
  }

  fecharModal(): void {
    this.produtoParaEdicaoId = null;
    this.exibirModalEdicao.set(false);
  }

  toggleFiltros(): void {
    this.exibirFiltrosAvancados.set(!this.exibirFiltrosAvancados());
  }

  // --- FORMATAÇÃO DE MOEDA NA DIGITAÇÃO ---
  formatarMoedaParaForm(event: Event, campo: 'precoCusto' | 'precoVenda'): void {
    const input = event.target as HTMLInputElement;

    if (input.value === '') {
      this.produtoForm.patchValue({ [campo]: null });
      if (campo === 'precoCusto') this.exibicaoCusto.set('');
      else this.exibicaoVenda.set('');
      return;
    }

    let valorNumStr = input.value.replace(/\D/g, '');
    if (!valorNumStr) valorNumStr = '0';

    const valorNumerico = Number(valorNumStr) / 100;
    const valorFormatado = AppFormatter.decimal(valorNumerico);

    if (campo === 'precoCusto') {
      this.exibicaoCusto.set(valorFormatado);
    } else {
      this.exibicaoVenda.set(valorFormatado);
    }

    this.produtoForm.patchValue({ [campo]: valorNumerico });
  }
}
