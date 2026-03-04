import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';
import { AlterarStatusEmpresaRequest, DashboardGlobalDto, EmpresaGlobalDto } from '../models/backoffice.model';

@Injectable({
  providedIn: 'root'
})
export class BackofficeService {
  private http = inject(HttpClient);

  private readonly API_EMPRESAS = 'http://localhost:8080/api/backoffice/empresas';
  private readonly API_DASHBOARD = 'http://localhost:8080/api/backoffice/dashboard';

  obterResumoDashboard(): Observable<ApiResponse<DashboardGlobalDto>> {
    return this.http.get<ApiResponse<DashboardGlobalDto>>(`${this.API_DASHBOARD}/resumo`);
  }

  // BUSCA SERVER-SIDE (Escalável para milhares de registros)
  listarEmpresas(termo?: string, pagina: number = 0, tamanho: number = 20): Observable<ApiResponse<{ content: EmpresaGlobalDto[], totalElements: number }>> {
    let params = new HttpParams()
      .set('page', pagina.toString())
      .set('size', tamanho.toString());

    if (termo) {
      params = params.set('termo', termo);
    }

    return this.http.get<ApiResponse<any>>(this.API_EMPRESAS, { params });
  }

  alterarStatusEmpresa(id: number, request: AlterarStatusEmpresaRequest): Observable<ApiResponse<EmpresaGlobalDto>> {
    return this.http.patch<ApiResponse<EmpresaGlobalDto>>(`${this.API_EMPRESAS}/${id}/status`, request);
  }

  // FUNCIONALIDADE ESTRATÉGICA: Gerar token temporário para suporte
  gerarTokenAcessoSuporte(empresaId: number): Observable<ApiResponse<{ token: string }>> {
    // CORREÇÃO: Variável na URL alterada de ${id} para ${empresaId}
    return this.http.post<ApiResponse<{ token: string }>>(`${this.API_EMPRESAS}/${empresaId}/impersonate`, {});
  }
}
