import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

export interface DespesaRequest {
  descricao: string;
  valor: number;
  tipo: string;
  data: string;
  ehPessoal: boolean;
}

@Injectable({ providedIn: 'root' })
export class DespesaService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/despesas';

  salvar(despesa: DespesaRequest): Observable<ApiResponse<any>> {
    const payload = { ...despesa };

    if (payload.data && !payload.data.includes('T')) {
      payload.data = `${payload.data}T00:00:00`;
    }

    return this.http.post<ApiResponse<any>>(this.API, payload);
  }

  listar(): Observable<ApiResponse<any[]>> {
    return this.http.get<ApiResponse<any[]>>(this.API);
  }

  excluir(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${id}`);
  }
}
