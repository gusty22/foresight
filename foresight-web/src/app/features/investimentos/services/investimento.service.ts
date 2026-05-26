import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

@Injectable({ providedIn: 'root' })
export class InvestimentoService {
  private http = inject(HttpClient);
  private API = 'http://localhost:8080/api/investimentos';

  listarInvestidores(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.API}/investidores`);
  }

  buscarInvestidorPorId(id: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.API}/investidores/${id}`);
  }

  salvarInvestidor(data: any): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/investidores`, data);
  }

  atualizarInvestidor(id: number, data: any): Observable<ApiResponse<any>> {
    return this.http.put<ApiResponse<any>>(`${this.API}/investidores/${id}`, data);
  }

  inativarInvestidor(id: number): Observable<ApiResponse<any>> {
    return this.http.delete<ApiResponse<any>>(`${this.API}/investidores/${id}`);
  }

  listarRepassesPendentes(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(`${this.API}/repasses/pendentes`);
  }
  obterRelatorioInvestidor(id: number): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.API}/investidores/${id}/relatorio`);
  }
  // Adicione este método
  pagarInvestidor(id: number, valor: number): Observable<ApiResponse<any>> {
    return this.http.post<ApiResponse<any>>(`${this.API}/investidores/${id}/pagar`, { valor });
  }
}
