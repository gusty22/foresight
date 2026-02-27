import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ApiResponse } from '../../../../core/http/api-response.model';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';

export interface DashboardGlobalDto {
  totalEmpresas: number;
  empresasAtivas: number;
  empresasSuspensas: number;
  faturamentoMensalEstimado: number;
  totalUsuariosGlobais: number;
}

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule, RouterLink, BrMaskPipe],
  templateUrl: './dashboard-admin.html',
  styleUrls: ['./dashboard-admin.scss']
})
export class DashboardAdminComponent implements OnInit {
  private http = inject(HttpClient);

  resumo = signal<DashboardGlobalDto | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);

  private readonly API = 'http://localhost:8080/api/backoffice/dashboard/resumo';

  ngOnInit(): void {
    this.carregarDadosGlobais();
  }

  carregarDadosGlobais(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<ApiResponse<DashboardGlobalDto>>(this.API).subscribe({
      next: (res) => {
        this.resumo.set(res.data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Falha de acesso ao Backoffice:', err);
        this.error.set('Não foi possível carregar os dados. Tente novamente.');
        this.loading.set(false);
      }
    });
  }
}