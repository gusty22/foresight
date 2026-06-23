import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { RelatorioAvancadoService } from '../../services/relatorio-avancado.service';
import { FiltroRelatorio, TransacaoRelatorio, RankingVendas, InadimplenciaRelatorio } from '../../models/relatorio-avancado.model';
import { BrMaskPipe } from '../../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-relatorio-geral',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './relatorio-geral.html',
  styleUrls: ['./relatorio-geral.scss']
})
export class RelatorioGeralComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private relatorioService = inject(RelatorioAvancadoService);
  private destroy$ = new Subject<void>();

  dados = signal<TransacaoRelatorio[]>([]);
  dadosRanking = signal<RankingVendas[]>([]);
  dadosInadimplencia = signal<InadimplenciaRelatorio[]>([]);

  totalElementos = signal<number>(0);
  loading = signal<boolean>(false);
  exportando = signal<boolean>(false);

  paginaAtual = signal<number>(0);
  tamanhoPagina = signal<number>(10);
  ordenarPor = signal<string>('dataHora');
  direcaoOrdem = signal<'asc' | 'desc'>('desc');
  tipoRelatorioSelecionado = signal<'FLUXO' | 'VENDAS' | 'RANKING' | 'INADIMPLENCIA'>('FLUXO');

  filtrosForm: FormGroup = this.fb.group({
    termoBusca: [''],
    dataInicio: [''],
    dataFim: [''],
    tipo: [''],
    categoria: ['']
  });

  totalPaginas = computed(() => Math.ceil(this.totalElementos() / this.tamanhoPagina()));
  totalEmRisco = computed(() => this.dadosInadimplencia().reduce((acc, curr) => acc + curr.valorDevido, 0));

  ngOnInit() {
    this.filtrosForm.valueChanges
      .pipe(
        debounceTime(400),
        distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.paginaAtual.set(0);
        this.carregarRelatorio();
      });

    this.carregarRelatorio();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  trocarContexto(tipo: 'FLUXO' | 'VENDAS' | 'RANKING' | 'INADIMPLENCIA') {
    if (this.tipoRelatorioSelecionado() === tipo) return;

    this.tipoRelatorioSelecionado.set(tipo);
    this.paginaAtual.set(0);
    this.dados.set([]);
    this.dadosRanking.set([]);
    this.dadosInadimplencia.set([]);

    this.filtrosForm.reset({ termoBusca: '', dataInicio: '', dataFim: '', tipo: '', categoria: '' }, { emitEvent: false });
    this.carregarRelatorio();
  }

  carregarRelatorio() {
    this.loading.set(true);

    if (this.tipoRelatorioSelecionado() === 'RANKING') {
      this.relatorioService.buscarRanking().subscribe({
        next: (res) => {
          this.dadosRanking.set(res.data || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
      return;
    }

    if (this.tipoRelatorioSelecionado() === 'INADIMPLENCIA') {
      this.relatorioService.buscarInadimplencia().subscribe({
        next: (res) => {
          this.dadosInadimplencia.set(res.data || []);
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });
      return;
    }

    const formValues = this.filtrosForm.value;
    const filtro: FiltroRelatorio = {
      contexto: this.tipoRelatorioSelecionado(),
      termoBusca: formValues.termoBusca,
      dataInicio: formValues.dataInicio,
      dataFim: formValues.dataFim,
      tipo: formValues.tipo,
      categoria: formValues.categoria,
      page: this.paginaAtual(),
      size: this.tamanhoPagina(),
      sort: `${this.ordenarPor()},${this.direcaoOrdem()}`
    };

    this.relatorioService.buscarDados(filtro).subscribe({
      next: (res) => {
        if(res.data) {
          this.dados.set(res.data.content);
          this.totalElementos.set(res.data.totalElements);
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  exportar() {
    this.exportando.set(true);
    const formValues = this.filtrosForm.value;

    const filtro: FiltroRelatorio = {
      ...formValues,
      contexto: this.tipoRelatorioSelecionado(),
      page: 0,
      size: 5000
    };

    this.relatorioService.exportarPdf(filtro).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `relatorio_${this.tipoRelatorioSelecionado().toLowerCase()}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.exportando.set(false);
      },
      error: () => {
        this.exportando.set(false);
        alert('Erro ao gerar relatório.');
      }
    });
  }

  mudarPagina(novaPagina: number) {
    if (novaPagina >= 0 && novaPagina < this.totalPaginas()) {
      this.paginaAtual.set(novaPagina);
      this.carregarRelatorio();
    }
  }

  ordenar(coluna: string) {
    if (this.ordenarPor() === coluna) {
      this.direcaoOrdem.set(this.direcaoOrdem() === 'asc' ? 'desc' : 'asc');
    } else {
      this.ordenarPor.set(coluna);
      this.direcaoOrdem.set('desc');
    }
    this.carregarRelatorio();
  }

  limparFiltros() {
    this.filtrosForm.reset({ termoBusca: '', dataInicio: '', dataFim: '', tipo: '', categoria: '' }, { emitEvent: false });
    this.paginaAtual.set(0);
    this.carregarRelatorio();
  }
}
