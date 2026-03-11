import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';
import { ProdutoDto } from '../models/produto.dto';

@Injectable({ providedIn: 'root' })
export class PrecificacaoService {
  private http = inject(HttpClient);
  private readonly API_PRODUTOS = 'http://localhost:8080/api/produtos';
  listarProdutosParaAnalise(): Observable<ApiResponse<ProdutoDto[]>> {
    return this.http.get<ApiResponse<ProdutoDto[]>>(this.API_PRODUTOS);
  }
}
