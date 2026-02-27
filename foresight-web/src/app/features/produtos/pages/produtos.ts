import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProdutoService, ProdutoDto } from '../services/produto.service';
import { BrMaskPipe, AppFormatter } from '../../../shared/pipes/br-mask.pipe'; // Importa Pipe e Classe Utilitária

@Component({
  selector: 'app-produtos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe], // Adiciona BrMaskPipe aos imports
  templateUrl: './produtos.html',
  styleUrls: ['./produtos.scss']
})
export class ProdutosComponent implements OnInit {
  private fb = inject(FormBuilder);
  private produtoService = inject(ProdutoService);

  listaOriginal = signal<ProdutoDto[]>([]);
  loading = signal(false);
  termoPesquisa = signal('');

  // Paginação
  paginaAtual = signal(1);
  itensPorPagina = signal(10);

  // Controle de Modal e Edição
  exibirModalEdicao = signal(false);
  produtoParaEdicaoId: number | null = null;

  // Controle de Exibição de Moeda nos Inputs (UX)
  exibicaoCusto = signal('');
  exibicaoVenda = signal('');

  produtoForm: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    precoCusto: [0, [Validators.required, Validators.min(0)]],
    precoVenda: [0, [Validators.required, Validators.min(0)]],
    estoqueAtual: [0, [Validators.required, Validators.min(0)]]
  });

  get f() { return this.produtoForm.controls; }

  ngOnInit() {
    this.listarProdutos();
  }

  // --- FILTROS E PAGINAÇÃO ---

  produtosFiltrados = computed(() => {
    const termo = this.termoPesquisa().toLowerCase();
    return this.listaOriginal().filter(p =>
      p.nome.toLowerCase().includes(termo) ||
      (p.alertaStatus && p.alertaStatus.toLowerCase().includes(termo))
    );
  });

  produtosPaginados = computed(() => {
    const inicio = (this.paginaAtual() - 1) * this.itensPorPagina();
    return this.produtosFiltrados().slice(inicio, inicio + this.itensPorPagina());
  });

  totalPaginas = computed(() => Math.ceil(this.produtosFiltrados().length / this.itensPorPagina()));

  // --- AÇÕES CRUD ---

  listarProdutos() {
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

  salvarProduto() {
    if (this.produtoForm.invalid) {
      this.produtoForm.markAllAsTouched();
      return;
    }

    const payload = this.produtoForm.value;

    if (this.produtoParaEdicaoId) {
      // Editar
      this.produtoService.atualizar(this.produtoParaEdicaoId, payload).subscribe({
        next: () => {
          this.fecharModal();
          this.listarProdutos();
        },
        error: (err) => console.error(err)
      });
    } else {
      // Criar Novo
      this.produtoService.criar(payload).subscribe({
        next: () => {
          this.produtoForm.reset({ estoqueAtual: 0, precoCusto: 0, precoVenda: 0 });
          this.exibicaoCusto.set('');
          this.exibicaoVenda.set('');
          this.listarProdutos();
        },
        error: (err) => console.error(err)
      });
    }
  }

  excluirProduto(id: number) {
    if (confirm('Tem certeza? Isso pode afetar o histórico de vendas.')) {
      this.produtoService.excluir(id).subscribe({
        next: () => this.listarProdutos(),
        error: (err) => console.error(err)
      });
    }
  }

  abrirEdicao(produto: ProdutoDto) {
    this.produtoParaEdicaoId = produto.id;

    // Popula o formulário
    this.produtoForm.patchValue({
      nome: produto.nome,
      precoCusto: produto.precoCusto,
      precoVenda: produto.precoVenda,
      estoqueAtual: produto.estoqueAtual
    });

    // Formata os valores para exibição no input usando AppFormatter (Decimal)
    this.exibicaoCusto.set(AppFormatter.decimal(produto.precoCusto));
    this.exibicaoVenda.set(AppFormatter.decimal(produto.precoVenda));

    this.exibirModalEdicao.set(true);
  }

  fecharModal() {
    this.produtoParaEdicaoId = null;
    this.produtoForm.reset({ estoqueAtual: 0 });
    this.exibicaoCusto.set('');
    this.exibicaoVenda.set('');
    this.exibirModalEdicao.set(false);
  }

  // --- MÁSCARA DE MOEDA (Input) ---

  formatarMoedaParaForm(event: Event, campo: 'precoCusto' | 'precoVenda') {
    const input = event.target as HTMLInputElement;
    let valorNumStr = input.value.replace(/\D/g, ''); // Remove tudo que não é dígito

    if (!valorNumStr) valorNumStr = '0';

    const valorNumerico = Number(valorNumStr) / 100; // Divide por 100 para ter centavos

    // Formata visualmente para o usuário (PT-BR)
    const valorFormatado = AppFormatter.decimal(valorNumerico);

    if (campo === 'precoCusto') {
      this.exibicaoCusto.set(valorFormatado);
    } else {
      this.exibicaoVenda.set(valorFormatado);
    }

    // Salva o número puro no FormControl
    this.produtoForm.patchValue({ [campo]: valorNumerico });
  }
}
