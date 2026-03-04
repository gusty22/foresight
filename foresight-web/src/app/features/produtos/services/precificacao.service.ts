import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';
import { ProdutoDto } from '../models/produto.dto'; // IMPORTAÇÃO CORRIGIDA E DESACOPLADA

@Injectable({ providedIn: 'root' })
export class PrecificacaoService {
  private http = inject(HttpClient);

  // Rota cega anti-IDOR. A segurança multi-tenant é garantida pelo JWT Interceptor
  private readonly API_PRODUTOS = 'http://localhost:8080/api/produtos';

  /**
   * Obtém os produtos do tenant atual para análise de precificação.
   */
  listarProdutosParaAnalise(): Observable<ApiResponse<ProdutoDto[]>> {
    return this.http.get<ApiResponse<ProdutoDto[]>>(this.API_PRODUTOS);
  }
}
