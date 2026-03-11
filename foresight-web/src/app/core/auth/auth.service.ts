import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, firstValueFrom } from 'rxjs';
import { LoginRequest, TokenResponse } from './models/auth.model';
import { ApiResponse } from '../http/api-response.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly ROLE_KEY = 'auth_role';

  token = signal<string | null>(localStorage.getItem(this.TOKEN_KEY));
  role = signal<string | null>(localStorage.getItem(this.ROLE_KEY));

  async login(dados: LoginRequest): Promise<ApiResponse<TokenResponse>> {
    const res = await firstValueFrom(this.http.post<ApiResponse<TokenResponse>>(`${this.API}/login`, dados));

    if (res.success && res.data?.token) {
      await this.salvarSessaoAssincrona(res.data.token);
    }
    return res;
  }

  logout(): void {
    localStorage.clear();
    sessionStorage.clear();
    this.token.set(null);
    this.role.set(null);
    this.router.navigateByUrl('/login');
  }

  estaLogado(): boolean {
    return !!this.token();
  }

  isSuperAdmin(): boolean {
    const r = this.role() || localStorage.getItem(this.ROLE_KEY);
    return r === 'ROLE_SUPER_ADMIN';
  }

  private async salvarSessaoAssincrona(jwt: string): Promise<void> {
    try {
      const payload = JSON.parse(atob(jwt.split('.')[1]));
      let r = payload.role || '';
      if (r && !r.startsWith('ROLE_')) r = `ROLE_${r}`;
      localStorage.setItem(this.TOKEN_KEY, jwt);
      localStorage.setItem(this.ROLE_KEY, r);

      this.token.set(jwt);
      this.role.set(r);
    } catch (e) {
      console.error('Falha crítica na decodificação do token');
      this.logout();
    }
  }
}
