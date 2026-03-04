import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

// ==========================================
// DTOs (Data Transfer Objects)
// Tipagem estrita e fechada. Previne Mass Assignment
// e garante que o frontend envie apenas o esperado.
// ==========================================
export interface ClienteRequest {
  nome: string;
  tipoCliente: 'PF' | 'PJ';
  statusCliente: 'ATIVO' | 'INATIVO' | 'INADIMPLENTE';
  // Campos opcionais explicitamente tipados para aceitar null do Reactive Forms
  documento?: string | null;
  telefone?: string | null;
  telefoneAlternativo?: string | null;
  email?: string | null;
  cidade?: string | null;
  estado?: string | null;
}

export interface ClienteDto extends ClienteRequest {
  id: number;
  dataCadastro?: string;
}

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private http = inject(HttpClient);

  // ROTA CEGA (Blindagem Multi-Tenant):
  // O Frontend não sabe de qual empresa é o dado. O JWT Interceptor anexa o Token
  // e o Backend (Spring Boot) injeta o tenant no TenantContext para filtrar o banco.
  private readonly API = 'http://localhost:8080/api/clientes';

  /**
   * Busca clientes de forma otimizada usando query params.
   * Evita injeção de caracteres especiais na rota (Path Traversal).
   */
  buscarPorTermo(termo: string): Observable<ApiResponse<ClienteDto[]>> {
    return this.http.get<ApiResponse<ClienteDto[]>>(`${this.API}/buscar`, {
      params: { termo }
    });
  }

  /**
   * Lista todos os clientes vinculados ao Tenant do usuário logado.
   */
  listar(): Observable<ApiResponse<ClienteDto[]>> {
    return this.http.get<ApiResponse<ClienteDto[]>>(this.API);
  }

  /**
   * Obtém detalhes de um cliente específico.
   * O Backend possui proteção Anti-IDOR para validar a posse do ID.
   */
  obterPorId(id: number): Observable<ApiResponse<ClienteDto>> {
    return this.http.get<ApiResponse<ClienteDto>>(`${this.API}/${id}`);
  }

  /**
   * Registra um novo cliente.
   */
  criar(cliente: ClienteRequest): Observable<ApiResponse<ClienteDto>> {
    return this.http.post<ApiResponse<ClienteDto>>(this.API, cliente);
  }

  /**
   * Atualiza os dados cadastrais.
   */
  atualizar(id: number, cliente: ClienteRequest): Observable<ApiResponse<ClienteDto>> {
    return this.http.put<ApiResponse<ClienteDto>>(`${this.API}/${id}`, cliente);
  }

  /**
   * Exclusão ou Inativação de Cliente.
   */
  excluir(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${id}`);
  }
}
