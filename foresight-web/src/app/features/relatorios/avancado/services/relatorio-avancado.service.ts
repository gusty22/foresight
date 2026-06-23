import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../../core/http/api-response.model';
import { FiltroRelatorio, PageResponse, TransacaoRelatorio } from '../models/relatorio-avancado.model';

@Injectable({ providedIn: 'root' })
export class RelatorioAvancadoService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/relatorios/avancado';

  private buildParams(filtro: FiltroRelatorio): HttpParams {
    let params = new HttpParams()
      .set('page', filtro.page.toString())
      .set('size', filtro.size.toString());

    if (filtro.contexto) params = params.set('contexto', filtro.contexto); // Garante a separação das abas
    if (filtro.termoBusca) params = params.set('termo', filtro.termoBusca);
    if (filtro.dataInicio) params = params.set('dataInicio', filtro.dataInicio);
    if (filtro.dataFim) params = params.set('dataFim', filtro.dataFim);
    if (filtro.tipo) params = params.set('tipo', filtro.tipo);
    if (filtro.categoria) params = params.set('categoria', filtro.categoria);
    if (filtro.sort) params = params.set('sort', filtro.sort);

    return params;
  }

  buscarDados(filtro: FiltroRelatorio): Observable<ApiResponse<PageResponse<TransacaoRelatorio>>> {
    return this.http.get<ApiResponse<PageResponse<TransacaoRelatorio>>>(this.API, { params: this.buildParams(filtro) });
  }

  exportarPdf(filtro: FiltroRelatorio): Observable<Blob> {
    return this.http.get(`${this.API}/export/pdf`, { params: this.buildParams(filtro), responseType: 'blob' });
  }
}
