import { Component, OnInit, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService, DashboardResumo } from '../services/dashboard.service';
import { BrMaskPipe } from '../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, BrMaskPipe],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  resumo = signal<DashboardResumo | null>(null);
  loading = signal<boolean>(true);
  erro = signal<boolean>(false);

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    this.loading.set(true);
    this.erro.set(false);

    this.dashboardService.obterResumo().subscribe({
      next: (res) => {
        this.resumo.set(res.data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar dashboard', err);
        this.erro.set(true);
        this.loading.set(false);
      }
    });
  }

  getProgressoWidth(): string {
    const p = this.resumo()?.percentualMeta || 0;
    return `${Math.min(p, 100)}%`;
  }
}
