import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../core/http/api-response.model';

export interface ItemVendaDto {
  produtoId: number;
  produtoNome?: string;
  quantidade: number;
  precoUnitario: number;
  subtotal?: number;
}

export interface VendaDto {
  id: number;
  clienteNome: string;
  clienteDocumento?: string;
  valorTotal: number;
  data: string;
  formaPagamento: string;
  statusPagamento: string;
  dataPagamento?: string;
  itens?: ItemVendaDto[];
}

export interface VendaRequest {
  cliente: {
    nome: string;
    documento?: string;
    telefone?: string;
  };
  itens: ItemVendaDto[];
  formaPagamento: string;
  status: string;
  dataPrevisao?: string;
}

@Injectable({ providedIn: 'root' })
export class VendaService {
  private http = inject(HttpClient);

  // Endpoint local para comunicar com o Spring Boot no desenvolvimento
  private readonly API = 'http://localhost:8080/api/vendas';

  realizarVenda(request: VendaRequest): Observable<ApiResponse<VendaDto>> {
    return this.http.post<ApiResponse<VendaDto>>(this.API, request);
  }

  listarHistorico(): Observable<ApiResponse<VendaDto[]>> {
    return this.http.get<ApiResponse<VendaDto[]>>(this.API);
  }

  buscarDetalhes(vendaId: number): Observable<ApiResponse<VendaDto>> {
    return this.http.get<ApiResponse<VendaDto>>(`${this.API}/${vendaId}/detalhes`);
  }

  confirmarPagamento(id: number): Observable<ApiResponse<VendaDto>> {
    return this.http.put<ApiResponse<VendaDto>>(`${this.API}/${id}/confirmar-pagamento`, {});
  }

  cancelarVenda(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API}/${id}`);
  }

  downloadComprovante(vendaId: number): Observable<Blob> {
    return this.http.get(`${this.API}/${vendaId}/comprovante`, { responseType: 'blob' });
  }
}
