import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';
import { AlterarStatusEmpresaRequest, EmpresaGlobalDto } from '../models/backoffice.model';

@Injectable({
  providedIn: 'root'
})
export class BackofficeService {
  private http = inject(HttpClient);
  // Caminho exclusivo da API Administrativa
  private readonly API = 'http://localhost:8080/api/backoffice/empresas';

  listarTodasEmpresas(): Observable<ApiResponse<EmpresaGlobalDto[]>> {
    return this.http.get<ApiResponse<EmpresaGlobalDto[]>>(this.API);
  }

  alterarStatusEmpresa(id: number, request: AlterarStatusEmpresaRequest): Observable<ApiResponse<EmpresaGlobalDto>> {
    return this.http.patch<ApiResponse<EmpresaGlobalDto>>(`${this.API}/${id}/status`, request);
  }
}
