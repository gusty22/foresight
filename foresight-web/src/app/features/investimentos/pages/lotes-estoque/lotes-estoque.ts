import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AppFormatter, BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-estoques-financiados', // Confirme se este seletor bate com a sua rota
  standalone: true,
  imports: [CommonModule, FormsModule, BrMaskPipe],
  templateUrl: './lotes-estoque.html' // Confirme se este é o nome exato do seu arquivo HTML
})
export class LotesEstoqueComponent implements OnInit {
  private http = inject(HttpClient);

  lotesOriginal = signal<any[]>([]);
  loading = signal(false);
  termoBusca = signal('');

  ngOnInit() {
    this.carregarLotes();
  }

  carregarLotes() {
    this.loading.set(true);
    // Verifique se esta URL está correta com a sua API do Spring Boot
    this.http.get<any>('http://localhost:8080/api/lotes').subscribe({
      next: (res) => {
        this.lotesOriginal.set(res.data || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  buscar(event: Event) {
    const input = event.target as HTMLInputElement;
    this.termoBusca.set(input.value.toLowerCase());
  }

  // Filtro que reage imediatamente
  lotesFiltrados = computed(() => {
    const termo = this.termoBusca();
    if (!termo) return this.lotesOriginal();

    return this.lotesOriginal().filter(l =>
      l.produtoNome.toLowerCase().includes(termo) ||
      l.investidorNome.toLowerCase().includes(termo)
    );
  });

  // KPI 1: Quantos lotes ainda têm mercadoria para vender?
  lotesAtivosCount = computed(() =>
    this.lotesOriginal().filter(l => l.status === 'ABERTO').length
  );

  // KPI 2: Qual o valor total em dinheiro que está parado no estoque financiado?
  capitalEmGiro = computed(() => {
    return this.lotesOriginal()
      .filter(l => l.status === 'ABERTO')
      .reduce((acc, lote) => acc + (lote.quantidadeDisponivel * lote.custoUnitario), 0);
  });
}
