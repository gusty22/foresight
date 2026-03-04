import { Component, OnInit, inject, signal, computed, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PrecificacaoService } from '../../services/precificacao.service';
import { ProdutoDto } from '../../models/produto.dto';
import { BrMaskPipe, AppFormatter } from '../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-inteligencia-preco',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './inteligencia-preco.html',
  styleUrls: ['./inteligencia-preco.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InteligenciaPrecoComponent implements OnInit {
  private fb = inject(FormBuilder);
  private precificacaoService = inject(PrecificacaoService);
  private destroyRef = inject(DestroyRef);

  // Estados Gerais
  loading = signal<boolean>(false);
  produtos = signal<ProdutoDto[]>([]);

  // =======================================================
  // LADO ESQUERDO: SIMULADOR DE PREÇO UNITÁRIO
  // =======================================================
  simuladorForm: FormGroup = this.fb.group({
    produtoId: [null],
    custoBase: [null, [Validators.required, Validators.min(0.01)]],
    taxasVariaveis: [0, [Validators.min(0), Validators.max(100)]],
    precoVenda: [null, [Validators.required, Validators.min(0.01)]]
  });

  exibicaoCusto = signal<string>('');
  exibicaoVenda = signal<string>('');

  custoTotal = signal<number>(0);
  lucroUnitario = signal<number>(0);
  margemLiquida = signal<number>(0);
  markup = signal<number>(0);

  // =======================================================
  // LADO DIREITO: ENGENHARIA DE METAS (MIX DE PRODUTOS)
  // =======================================================
  metaGlobalValor = signal<number>(0);
  exibicaoMetaGlobal = signal<string>('');
  produtosMixIds = signal<number[]>([]); // Guarda os IDs selecionados para a meta

  // Produtos aptos para compor meta (Tem que ter lucro > 0)
  produtosElegiveisParaMeta = computed(() => {
    const idsJaAdicionados = this.produtosMixIds();
    return this.produtos().filter(p => p.lucroReal > 0 && !idsJaAdicionados.includes(p.id));
  });

  // Motor de Cálculo do Mix
  planejamentoMix = computed(() => {
    const meta = this.metaGlobalValor();
    const ids = this.produtosMixIds();
    const produtosNoMix = this.produtos().filter(p => ids.includes(p.id));

    if (meta <= 0 || produtosNoMix.length === 0) {
      return null;
    }

    // Estratégia de negócio: Divide o esforço de lucro igualmente entre o mix
    const metaPorProduto = meta / produtosNoMix.length;
    let faturamentoProjetadoGeral = 0;

    const detalhes = produtosNoMix.map(p => {
      // Teto para garantir que não falte centavos na meta
      const qtdNecessaria = Math.ceil(metaPorProduto / p.lucroReal);
      const faturamentoGerado = qtdNecessaria * p.precoVenda;

      faturamentoProjetadoGeral += faturamentoGerado;

      return {
        produto: p,
        qtdNecessaria,
        faturamentoGerado,
        metaAlocada: metaPorProduto
      };
    });

    return {
      itens: detalhes,
      faturamentoProjetadoGeral,
      metaOriginal: meta
    };
  });


  ngOnInit(): void {
    this.carregarPortfolio();
    this.escutarSimulador();
  }

  carregarPortfolio(): void {
    this.loading.set(true);
    this.precificacaoService.listarProdutosParaAnalise().subscribe({
      next: (res) => {
        this.produtos.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar portfólio', err);
        this.loading.set(false);
      }
    });
  }

  // --- MÉTODOS DO SIMULADOR (Lado Esquerdo) ---
  aoSelecionarProdutoSimulador(event: Event): void {
    const target = event.target as HTMLSelectElement;
    if (!target.value || target.value === 'null') {
      this.simuladorForm.reset({ taxasVariaveis: 0 });
      this.exibicaoCusto.set('');
      this.exibicaoVenda.set('');
      return;
    }

    const prod = this.produtos().find(p => p.id === Number(target.value));
    if (prod) {
      this.simuladorForm.patchValue({ custoBase: prod.precoCusto, precoVenda: prod.precoVenda, taxasVariaveis: 0 });
      this.exibicaoCusto.set(AppFormatter.decimal(prod.precoCusto));
      this.exibicaoVenda.set(AppFormatter.decimal(prod.precoVenda));
    }
  }

  private escutarSimulador(): void {
    this.simuladorForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(valores => {
        const custo = valores.custoBase || 0;
        const venda = valores.precoVenda || 0;
        const taxasPercentual = (valores.taxasVariaveis || 0) / 100;

        if (custo === 0 && venda === 0) {
          this.resetarResultadosSimulador();
          return;
        }

        const valorTaxas = venda * taxasPercentual;
        const custoFinal = custo + valorTaxas;
        const lucro = venda - custoFinal;

        this.custoTotal.set(custoFinal);
        this.lucroUnitario.set(lucro);
        this.margemLiquida.set(venda > 0 ? (lucro / venda) * 100 : 0);
        this.markup.set(custoFinal > 0 ? (lucro / custoFinal) * 100 : 0);
      });
  }

  private resetarResultadosSimulador(): void {
    this.custoTotal.set(0);
    this.lucroUnitario.set(0);
    this.margemLiquida.set(0);
    this.markup.set(0);
  }

  obterStatusSimulacao() {
    const margem = this.margemLiquida();
    if (margem < 0) return { cor: 'danger', texto: 'Prejuízo! Revise urgente.', icone: 'bi-x-octagon-fill' };
    if (margem < 15) return { cor: 'warning', texto: 'Margem Risco (<15%)', icone: 'bi-exclamation-triangle-fill' };
    if (margem < 35) return { cor: 'info', texto: 'Margem Saudável', icone: 'bi-check-circle-fill' };
    return { cor: 'success', texto: 'Alta Lucratividade', icone: 'bi-star-fill' };
  }

  // --- MÉTODOS DO PLANEJADOR DE MIX (Lado Direito) ---
  formatarMetaGlobal(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.value === '') {
      this.exibicaoMetaGlobal.set('');
      this.metaGlobalValor.set(0);
      return;
    }
    let valorNumStr = input.value.replace(/\D/g, '');
    if (!valorNumStr) valorNumStr = '0';

    const valorNumerico = Number(valorNumStr) / 100;
    this.exibicaoMetaGlobal.set(AppFormatter.decimal(valorNumerico));
    this.metaGlobalValor.set(valorNumerico);
  }

  adicionarProdutoAoMix(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const id = Number(select.value);

    if (id) {
      this.produtosMixIds.update(ids => [...ids, id]);
      select.value = ''; // Reseta o select após adicionar
    }
  }

  removerProdutoDoMix(id: number): void {
    this.produtosMixIds.update(ids => ids.filter(prodId => prodId !== id));
  }

  limparMix(): void {
    this.produtosMixIds.set([]);
  }

  // --- HELPER DE MOEDA GERAL ---
  formatarMoedaSimulador(event: Event, campo: string): void {
    const input = event.target as HTMLInputElement;
    if (input.value === '') {
      this.simuladorForm.patchValue({ [campo]: null });
      if (campo === 'custoBase') this.exibicaoCusto.set('');
      else this.exibicaoVenda.set('');
      return;
    }
    let valorNumStr = input.value.replace(/\D/g, '');
    if (!valorNumStr) valorNumStr = '0';

    const valorNumerico = Number(valorNumStr) / 100;
    if (campo === 'custoBase') this.exibicaoCusto.set(AppFormatter.decimal(valorNumerico));
    else this.exibicaoVenda.set(AppFormatter.decimal(valorNumerico));
    this.simuladorForm.patchValue({ [campo]: valorNumerico });
  }
}
