import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

// ARQUITETURA ENTERPRISE: Única Fonte da Verdade.
// Importamos o DTO isolado e não recriamos as interfaces aqui dentro.
import { ProdutoDto, ProdutoRequest } from '../models/produto.dto';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private http = inject(HttpClient);

  // Rota Cega (Anti-IDOR e Multi-Tenant seguro)
  // O ID da empresa NUNCA trafega na URL. O backend extrai do JWT (Bearer Token).
  private readonly API = 'http://localhost:8080/api/produtos';
  private readonly RELATORIOS_API = 'http://localhost:8080/api/relatorios';

  // ==========================================
  // CRUD DE PRODUTOS
  // ==========================================

  criar(produto: ProdutoRequest): Observable<ApiResponse<ProdutoDto>> {
    return this.http.post<ApiResponse<ProdutoDto>>(this.API, produto);
  }

  listar(): Observable<ApiResponse<ProdutoDto[]>> {
    return this.http.get<ApiResponse<ProdutoDto[]>>(this.API);
  }

  buscarPorId(id: number): Observable<ApiResponse<ProdutoDto>> {
    return this.http.get<ApiResponse<ProdutoDto>>(`${this.API}/${id}`);
  }

  atualizar(id: number, produto: ProdutoRequest): Observable<ApiResponse<ProdutoDto>> {
    return this.http.put<ApiResponse<ProdutoDto>>(`${this.API}/${id}`, produto);
  }

  excluir(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${id}`);
  }

  // ==========================================
  // ANÁLISE E RELATÓRIOS
  // ==========================================

  obterLucratividade(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.RELATORIOS_API}/lucratividade`);
  }

  obterSaudeFinanceira(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.RELATORIOS_API}/saude-financeira`);
  }
}
