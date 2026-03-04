import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';
import { AuditoriaLogDto } from '../models/auditoria.model';

@Injectable({ providedIn: 'root' })
export class AuditoriaService {
  private http = inject(HttpClient);

  // DEVE CONTER O TRECHO /backoffice/
  // Deve ter o /backoffice/ no meio!
  private readonly API = 'http://localhost:8080/api/backoffice/auditoria';

  listarLogs(pagina: number, tamanho: number, termo?: string, acao?: string, dataInicio?: string, dataFim?: string): Observable<ApiResponse<{ content: AuditoriaLogDto[], totalElements: number }>> {
    let params = new HttpParams().set('page', pagina.toString()).set('size', tamanho.toString());
    if (termo) params = params.set('termo', termo);
    if (acao && acao !== 'TODAS') params = params.set('acao', acao);
    if (dataInicio) params = params.set('dataInicio', dataInicio);
    if (dataFim) params = params.set('dataFim', dataFim);

    return this.http.get<ApiResponse<any>>(this.API, { params });
  }
}
