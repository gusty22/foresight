import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VendaService, VendaDto } from '../../services/venda.service';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-historico-vendas',
  standalone: true,
  imports: [CommonModule, BrMaskPipe],
  templateUrl: './historico-vendas.html',
  styleUrls: ['./historico-vendas.scss']
})
export class HistoricoVendasComponent implements OnInit {
  private vendaService = inject(VendaService);

  vendas = signal<VendaDto[]>([]);
  vendaSelecionada = signal<VendaDto | null>(null);
  exibirModal = signal(false);
  loading = signal(true);
  erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    this.loading.set(true);
    this.erro.set(null);

    this.vendaService.listarHistorico().subscribe({
      next: (res: any) => {
        this.vendas.set(res.data || []);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Erro ao carregar o histórico de vendas.');
        this.loading.set(false);
      }
    });
  }

  confirmarPagamento(venda: VendaDto): void {
    if (venda.statusPagamento === 'PAGO') return;

    if (confirm(`Deseja confirmar o recebimento de R$ ${venda.valorTotal} referente à venda #${venda.id}?`)) {
      this.loading.set(true);
      this.vendaService.confirmarPagamento(venda.id).subscribe({
        next: () => {
          this.carregarHistorico();
          if (this.exibirModal()) this.fecharModal();
        },
        error: () => {
          alert('Erro ao confirmar pagamento.');
          this.loading.set(false);
        }
      });
    }
  }

  cancelarVenda(id: number): void {
    if (confirm('Atenção: O valor será estornado do caixa e os itens voltarão ao estoque. Deseja prosseguir?')) {
      this.loading.set(true);
      this.vendaService.cancelarVenda(id).subscribe({
        next: () => {
          this.fecharModal();
          this.carregarHistorico();
        },
        error: () => {
          alert('Erro ao cancelar a venda e reverter o estoque.');
          this.loading.set(false);
        }
      });
    }
  }

  verDetalhes(vendaId: number): void {
    this.loading.set(true);
    this.vendaService.buscarDetalhes(vendaId).subscribe({
      next: (res: any) => {
        this.vendaSelecionada.set(res.data);
        this.exibirModal.set(true);
        this.loading.set(false);
      },
      error: () => {
        alert('Erro ao buscar detalhes desta venda.');
        this.loading.set(false);
      }
    });
  }

  fecharModal(): void {
    this.exibirModal.set(false);
    this.vendaSelecionada.set(null);
  }

  baixarNotaFiscal(vendaId: number): void {
    this.vendaService.downloadComprovante(vendaId).subscribe({
      next: (blob: Blob) => {
        const fileURL = URL.createObjectURL(blob);
        window.open(fileURL, '_blank');
      },
      error: () => {
        alert('Erro ao gerar PDF. Verifique se a venda pertence à sua empresa.');
      }
    });
  }
}
