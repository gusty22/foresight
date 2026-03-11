import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';
import { ClienteDto, ClienteRequest, ViaCepResponse } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);
  // Em produção, o host (http://localhost:8080) deve ser injetado por um HttpInterceptor
  private readonly API = '/api/clientes';

  buscarPorTermo(termo: string): Observable<ApiResponse<ClienteDto[]>> {
    return this.http.get<ApiResponse<ClienteDto[]>>(`${this.API}/buscar`, {
      params: { termo }
    });
  }

  listar(): Observable<ApiResponse<ClienteDto[]>> {
    return this.http.get<ApiResponse<ClienteDto[]>>(this.API);
  }

  obterPorId(id: number): Observable<ApiResponse<ClienteDto>> {
    return this.http.get<ApiResponse<ClienteDto>>(`${this.API}/${id}`);
  }

  criar(cliente: ClienteRequest): Observable<ApiResponse<ClienteDto>> {
    return this.http.post<ApiResponse<ClienteDto>>(this.API, cliente);
  }

  atualizar(id: number, cliente: ClienteRequest): Observable<ApiResponse<ClienteDto>> {
    return this.http.put<ApiResponse<ClienteDto>>(`${this.API}/${id}`, cliente);
  }

  excluir(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${id}`);
  }

  // Integração com API Externa Isola o Componente de chamadas diretas
  buscarCep(cep: string): Observable<ViaCepResponse | null> {
    const cepLimpo = cep.replace(/\D/g, '');
    if (cepLimpo.length !== 8) return of(null);

    return this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${cepLimpo}/json/`).pipe(
      // Tratamento defensivo: Se o ViaCEP cair, não quebra nossa aplicação
      catchError(() => of(null))
    );
  }
}
