import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

@Injectable({ providedIn: 'root' })
export class FluxoCaixaService {
  private http = inject(HttpClient);
  // URL SEGURA: O Backend resolve qual é a empresa através do JWT.
  private readonly API = 'http://localhost:8080/api/fluxo-caixa';

  listarHistorico(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(this.API);
  }
}
