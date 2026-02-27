import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe'; // Importa Pipe

@Component({
  selector: 'app-lucratividade',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, BrMaskPipe], // Adiciona Pipe
  templateUrl: './lucratividade.html'
})
export class LucratividadeComponent implements OnInit {
  private http = inject(HttpClient);

  relatorio = signal<any[]>([]);
  saudeFinanceira = signal<any>({ disponivelParaVoce: 0, despesasComprometidas: 0 });

  simulador = { custo: 0, preco: 0, resultado: null as any };
  metas = { prolaboreDesejado: 2000, produtoSelecionado: null as any, unidadesNecessarias: 0 };

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    // Ajustar para usar o service centralizado futuramente
    this.http.get<any[]>('http://localhost:8080/api/relatorios/lucratividade')
      .subscribe((res) => this.relatorio.set(res));

    // Endpoint mockado ou real
    // this.http.get...
  }

  simularImpacto() {
    this.http.get<any>('http://localhost:8080/api/relatorios/simulador', {
      params: {
        custo: this.simulador.custo.toString(),
        precoVenda: this.simulador.preco.toString(),
        // Se não tiver despesas reais, usa 0
        fixas: (this.saudeFinanceira().despesasComprometidas || 0).toString()
      }
    }).subscribe((res) => this.simulador.resultado = res);
  }

  calcularMetaUnidades() {
    if (this.metas.produtoSelecionado) {
      const lucroUnitario = this.metas.produtoSelecionado.lucroUnitario;
      if (lucroUnitario > 0) {
        this.metas.unidadesNecessarias = Math.ceil(this.metas.prolaboreDesejado / lucroUnitario);
      } else {
        this.metas.unidadesNecessarias = 0;
      }
    }
  }
}
