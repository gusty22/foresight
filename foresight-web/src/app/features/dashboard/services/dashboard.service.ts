import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

export interface MovimentacaoRecente {
  descricao: string;
  categoria: string;
  valor: number;
  tipo: 'ENTRADA' | 'SAIDA';
  data: string;
}

export interface DashboardResumo {
  faturamentoMes: number;
  lucroLiquido: number;
  estoqueCritico: number;
  saldoCaixa: number;
  metaSobrevivencia: number;
  percentualMeta: number;
  movimentacoesRecentes: MovimentacaoRecente[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/dashboard';

  obterResumo(): Observable<ApiResponse<DashboardResumo>> {
    return this.http.get<ApiResponse<DashboardResumo>>(`${this.API}/resumo`);
  }
}
