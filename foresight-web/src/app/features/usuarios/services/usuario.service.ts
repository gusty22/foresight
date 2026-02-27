import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// DTOs alinhados com o Back-end
export interface EmpresaDto {
  id: number;
  nome: string;
  cnpj: string | null;
  endereco: string | null; // CORREÇÃO: Propriedade adicionada
  prolaboreDesejado: number;
  tipo: string;
}

export interface PerfilUsuarioDto {
  id: number;
  nome: string;
  email: string;
  telefone: string | null;
  empresa: EmpresaDto | null;
}

export interface AtualizarPerfilRequest {
  nome: string;
  telefone: string | null;
}

export interface AtualizarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private http = inject(HttpClient);
  // URL base alinhada com o UsuarioController
  private apiUrl = 'http://localhost:8080/api/usuarios/me';

  // GET /api/usuarios/me
  obterMeuPerfil(): Observable<PerfilUsuarioDto> {
    return this.http.get<PerfilUsuarioDto>(this.apiUrl);
  }

  // PUT /api/usuarios/me/perfil
  atualizarMeuPerfil(request: AtualizarPerfilRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/perfil`, request);
  }

  // PUT /api/usuarios/me/senha
  atualizarMinhaSenha(request: AtualizarSenhaRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/senha`, request);
  }
}
