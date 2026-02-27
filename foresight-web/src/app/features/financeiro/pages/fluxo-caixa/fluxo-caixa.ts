import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FluxoCaixaService } from '../../services/fluxo-caixa.service';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe'; // Pipe Import

@Component({
  selector: 'app-fluxo-caixa',
  standalone: true,
  imports: [CommonModule, BrMaskPipe], // Declaração do Pipe
  templateUrl: './fluxo-caixa.html',
  styleUrl: './fluxo-caixa.scss'
})
export class FluxoCaixaComponent implements OnInit {
  private fluxoService = inject(FluxoCaixaService);

  movimentacoes = signal<any[]>([]);
  saldoTotal = signal(0);
  loading = signal(true);

  ngOnInit() {
    this.carregarFluxo();
  }

  carregarFluxo() {
    this.loading.set(true);
    // Chamada isolada (sem buscar ID manual no Front)
    this.fluxoService.listarHistorico().subscribe({
      next: (res) => {
        const dados = res.data || [];
        this.movimentacoes.set(dados);

        const saldo = dados.reduce((acc: number, item: any) => {
          return item.tipo === 'ENTRADA' ? acc + item.valor : acc - item.valor;
        }, 0);
        this.saldoTotal.set(saldo);
        this.loading.set(false);
      },
      error: (err) => {
        console.error("Erro ao carregar fluxo", err);
        this.loading.set(false);
      }
    });
  }
}
