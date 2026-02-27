import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
// O import do environment foi removido para evitar o erro TS2307
import { EmpresaRequest, EmpresaDto } from '../models/empresa.model';
import { ApiResponse } from '../../../core/http/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class EmpresaService {
  private http = inject(HttpClient);
  // URL Hardcoded temporariamente para o MVP local (o ideal é ter a URL em um app.config ou environment criado)
  private apiUrl = 'http://localhost:8080/api/empresas';

  criar(request: EmpresaRequest): Observable<ApiResponse<EmpresaDto>> {
    return this.http.post<ApiResponse<EmpresaDto>>(this.apiUrl, request);
  }

  listar(): Observable<ApiResponse<EmpresaDto[]>> {
    return this.http.get<ApiResponse<EmpresaDto[]>>(this.apiUrl);
  }

  buscarPorId(id: number): Observable<ApiResponse<EmpresaDto>> {
    return this.http.get<ApiResponse<EmpresaDto>>(`${this.apiUrl}/${id}`);
  }

  atualizar(id: number, request: EmpresaRequest): Observable<ApiResponse<EmpresaDto>> {
    return this.http.put<ApiResponse<EmpresaDto>>(`${this.apiUrl}/${id}`, request);
  }

  excluir(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
