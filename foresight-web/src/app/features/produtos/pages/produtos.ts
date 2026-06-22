import { Component, OnInit, inject, signal, computed, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ProdutoService } from '../services/produto.service';
import { InvestimentoService } from '../../investimentos/services/investimento.service';
import { ProdutoDto, ProdutoRequest } from '../models/produto.dto';
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
  private investimentoService = inject(InvestimentoService);
  private http = inject(HttpClient);
  private destroyRef = inject(DestroyRef);

  listaOriginal = signal<ProdutoDto[]>([]);
  categorias = signal<any[]>([]);
  investidores = signal<any[]>([]);

  loading = signal(false);
  salvando = signal(false);

  paginaAtual = signal(1);
  itensPorPagina = signal(10);
  exibirModalEdicao = signal(false);
  produtoParaEdicaoId: number | null = null;
  exibicaoCusto = signal('');
  exibicaoVenda = signal('');
  exibirFiltrosAvancados = signal(false);
  filtrosAtivos = signal({ termo: '', categoriaId: '', status: 'TODOS', limiteEstoque: null as number | null });

  filtroForm: FormGroup = this.fb.group({
    termo: [''], categoriaId: [''], status: ['TODOS'], limiteEstoque: [null]
  });

  produtoForm: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    categoriaId: [null],
    fornecedorNome: ['', [Validators.maxLength(150)]],
    precoCusto: [null, [Validators.required, Validators.min(0)]],
    precoVenda: [null, [Validators.required, Validators.min(0)]],
    estoqueAtual: [null, [Validators.required, Validators.min(0)]],
    estoqueMinimo: [5, [Validators.required, Validators.min(1)]],
    temInvestidor: [false],
    investidorId: [null],
    percentualLucroInvestidor: [null, [Validators.min(0), Validators.max(100)]]
  });

  get f() { return this.produtoForm.controls; }

  ngOnInit(): void {
    this.carregarApoio();
    this.listarProdutos();
    this.escutarMudancasDeFiltro();
    this.escutarToggleInvestidor();
  }

  carregarApoio(): void {
    // Categorias pré-definidas para facilitar a apresentação (Hardcoded)
    this.categorias.set([
      { id: 1, nome: 'Eletrônicos e Celulares' },
      { id: 2, nome: 'Acessórios de Informática' },
      { id: 3, nome: 'Móveis e Decoração' },
      { id: 4, nome: 'Papelaria e Escritório' },
      { id: 5, nome: 'Outros' }
    ]);

    // Busca investidores ativos normalmente
    this.investimentoService.listarInvestidores().subscribe(res => {
      const ativos = (res.data || []).filter((i: any) => i.status === 'ATIVO');
      this.investidores.set(ativos);
    });
  }

  private escutarToggleInvestidor(): void {
    this.produtoForm.get('temInvestidor')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(isFinanciado => {
        const invCtrl = this.produtoForm.get('investidorId');
        const percCtrl = this.produtoForm.get('percentualLucroInvestidor');

        if (isFinanciado) {
          invCtrl?.setValidators([Validators.required]);
          percCtrl?.setValidators([Validators.required, Validators.min(0.1), Validators.max(100)]);
        } else {
          invCtrl?.clearValidators();
          percCtrl?.clearValidators();
          invCtrl?.setValue(null);
          percCtrl?.setValue(null);
        }
        invCtrl?.updateValueAndValidity();
        percCtrl?.updateValueAndValidity();
      });
  }

  private escutarMudancasDeFiltro(): void {
    this.filtroForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(valores => {
        this.filtrosAtivos.set({
          termo: (valores.termo || '').toLowerCase(),
          categoriaId: valores.categoriaId || '',
          status: valores.status || 'TODOS',
          limiteEstoque: valores.limiteEstoque
        });
        this.paginaAtual.set(1);
      });
  }

  produtosFiltrados = computed(() => {
    const { termo, categoriaId, status, limiteEstoque } = this.filtrosAtivos();

    return this.listaOriginal().filter(p => {
      const matchNome = p.nome.toLowerCase().includes(termo) || (p.codigoBarras && p.codigoBarras.includes(termo));
      const matchCat = categoriaId ? String(p.categoriaId) === String(categoriaId) : true;
      const limite = p.estoqueMinimo || 5;
      const isCritico = p.estoqueAtual <= limite;
      const matchStatus = status === 'TODOS' || (status === 'BAIXO' && isCritico) || (status === 'OK' && !isCritico);
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
    this.filtroForm.reset({ status: 'TODOS', categoriaId: '' });
  }

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
    if (!this.produtoForm.get('temInvestidor')?.value) {
      this.produtoForm.patchValue({ investidorId: null, percentualLucroInvestidor: null });
    }

    if (this.produtoForm.invalid) {
      this.produtoForm.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    const formValue = this.produtoForm.value;

    const payload: ProdutoRequest = {
      nome: formValue.nome,
      codigoBarras: null, // Simplificado para a apresentação
      imagemUrl: null, // Simplificado para a apresentação
      categoriaId: formValue.categoriaId || null,
      fornecedorId: null, // Não enviamos o ID pois a tela possui apenas um campo de texto agora
      precoCusto: formValue.precoCusto || 0,
      precoVenda: formValue.precoVenda || 0,
      estoqueAtual: formValue.estoqueAtual || 0,
      estoqueMinimo: formValue.estoqueMinimo || 5,
      investidorId: formValue.temInvestidor ? formValue.investidorId : null,
      percentualLucroInvestidor: formValue.temInvestidor ? formValue.percentualLucroInvestidor : null
    };

    if (this.produtoParaEdicaoId) {
      this.produtoService.atualizar(this.produtoParaEdicaoId, payload).subscribe({
        next: () => this.concluirSalvamento(),
        error: (err) => {
          console.error(err.error?.message || 'Erro ao salvar produto');
          this.salvando.set(false);
        }
      });
    } else {
      this.produtoService.criar(payload).subscribe({
        next: () => this.concluirSalvamento(),
        error: (err) => {
          console.error(err.error?.message || 'Erro ao criar produto');
          this.salvando.set(false);
        }
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

  abrirNovoProduto(): void {
    this.produtoParaEdicaoId = null;
    this.produtoForm.reset({ estoqueMinimo: 5, categoriaId: '', fornecedorNome: '', temInvestidor: false });
    this.exibicaoCusto.set('');
    this.exibicaoVenda.set('');
    this.exibirModalEdicao.set(true);
  }

  abrirEdicao(produto: any): void {
    this.produtoParaEdicaoId = produto.id;
    const temInvestidor = !!(produto.investidorId && produto.percentualLucroInvestidor);

    this.produtoForm.patchValue({
      nome: produto.nome,
      categoriaId: produto.categoriaId || '',
      fornecedorNome: produto.fornecedorNome || '',
      precoCusto: produto.precoCusto,
      precoVenda: produto.precoVenda,
      estoqueAtual: produto.estoqueAtual,
      estoqueMinimo: produto.estoqueMinimo || 5,
      temInvestidor: temInvestidor,
      investidorId: produto.investidorId || null,
      percentualLucroInvestidor: produto.percentualLucroInvestidor || null
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
    if (campo === 'precoCusto') this.exibicaoCusto.set(valorFormatado);
    else this.exibicaoVenda.set(valorFormatado);
    this.produtoForm.patchValue({ [campo]: valorNumerico });
  }
}
