import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

// DTOs embutidos para garantir a tipagem estrita e evitar o erro "any"
export interface ProdutoRequest {
  nome: string;
  precoCusto: number;
  precoVenda: number;
  estoqueAtual: number;
}

export interface ProdutoDto extends ProdutoRequest {
  id: number;
  lucroReal: number;
  margemReal: number;
  alertaStatus: string;
}

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private http = inject(HttpClient);

  // Rotas Base Isoladas
  private readonly API = 'http://localhost:8080/api/produtos';
  private readonly RELATORIOS_API = 'http://localhost:8080/api/relatorios';

  // ==========================================
  // CRUD DE PRODUTOS (Padrão REST Seguro)
  // O backend valida se o 'id' do produto pertence ao Tenant logado.
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
  // ANÁLISE E RELATÓRIOS (Sem IDOR)
  // O Token JWT garante o isolamento por empresa.
  // ==========================================

  obterLucratividade(): Observable<ApiResponse<any>> {
    // Rota limpa, sem anexar o ID da empresa no final
    return this.http.get<ApiResponse<any>>(`${this.RELATORIOS_API}/lucratividade`);
  }

  obterSaudeFinanceira(): Observable<ApiResponse<any>> {
    // Rota limpa, sem anexar o ID da empresa no final
    return this.http.get<ApiResponse<any>>(`${this.RELATORIOS_API}/saude-financeira`);
  }
}
