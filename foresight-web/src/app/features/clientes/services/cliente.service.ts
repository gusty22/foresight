import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

// DTOs embutidos para Blindagem e Tipagem Estrita
export interface ClienteRequest {
  nome: string;
  documento?: string;
  tipoCliente: 'PF' | 'PJ';
  telefone?: string;
  telefoneAlternativo?: string;
  email?: string;
  statusCliente: 'ATIVO' | 'INATIVO' | 'INADIMPLENTE';
}

export interface ClienteDto extends ClienteRequest {
  id: number;
  dataCadastro: string;
}

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);

  // Rota Cega: O ID da Empresa NUNCA é passado. O Interceptor JWT trata a segurança.
  private readonly API = 'http://localhost:8080/api/clientes';

  buscarPorTermo(termo: string): Observable<ApiResponse<ClienteDto[]>> {
    // Parâmetros construídos via objeto evitam falhas de encoding na URL
    return this.http.get<ApiResponse<ClienteDto[]>>(`${this.API}/buscar`, { params: { termo } });
  }

  listarTodos(): Observable<ApiResponse<ClienteDto[]>> {
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
}
