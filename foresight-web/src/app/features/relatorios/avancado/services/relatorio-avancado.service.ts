import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../../core/http/api-response.model';
import { FiltroRelatorio, PageResponse, TransacaoRelatorio, RankingVendas, InadimplenciaRelatorio } from '../models/relatorio-avancado.model';

@Injectable({ providedIn: 'root' })
export class RelatorioAvancadoService {
  private http = inject(HttpClient);
  // Atenção: a base da API agora é genérica para atender todas as abas
  private readonly API = 'http://localhost:8080/api/relatorios';

  private buildParams(filtro: FiltroRelatorio): HttpParams {
    let params = new HttpParams()
      .set('page', filtro.page.toString())
      .set('size', filtro.size.toString());

    if (filtro.contexto) params = params.set('contexto', filtro.contexto);
    if (filtro.termoBusca) params = params.set('termo', filtro.termoBusca);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    if (filtro.tipo) params = params.set('tipo', filtro.tipo);
    if (filtro.categoria) params = params.set('categoria', filtro.categoria);
    if (filtro.sort) params = params.set('sort', filtro.sort);

    return params;
  }

  // 1. Aba Fluxo de Caixa e Vendas (Paginado)
  buscarDados(filtro: FiltroRelatorio): Observable<ApiResponse<PageResponse<TransacaoRelatorio>>> {
    return this.http.get<ApiResponse<PageResponse<TransacaoRelatorio>>>(`${this.API}/avancado`, { params: this.buildParams(filtro) });
  }

  // Exportação PDF (Para Fluxo e Vendas)
  exportarPdf(filtro: FiltroRelatorio): Observable<Blob> {
    return this.http.get(`${this.API}/avancado/export/pdf`, { params: this.buildParams(filtro), responseType: 'blob' });
  }

  // 2. Aba Ranking de Vendas
  buscarRanking(): Observable<ApiResponse<RankingVendas[]>> {
    return this.http.get<ApiResponse<RankingVendas[]>>(`${this.API}/ranking-vendas`);
  }

  // 3. Aba Inadimplência
  buscarInadimplencia(): Observable<ApiResponse<InadimplenciaRelatorio[]>> {
    return this.http.get<ApiResponse<InadimplenciaRelatorio[]>>(`${this.API}/inadimplencia`);
  }
}
