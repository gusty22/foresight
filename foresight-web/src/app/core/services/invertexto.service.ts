import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class InvertextoService {
  private http = inject(HttpClient);
  // Substitua pelo seu token real
  private readonly TOKEN = '26461|a4gdiwXxFUgNbPYxSUGVX9AXMl9iTjxZ';
  private readonly URL = 'https://api.invertexto.com/v1';

  validarDocumento(documento: string): Observable<boolean> {
    const cleanDoc = documento.replace(/\D/g, '');
    if (!cleanDoc) return of(false);

    return this.http.get<any>(`${this.URL}/validator?token=${this.TOKEN}&value=${cleanDoc}`).pipe(
      map(res => res.valid === true),
      catchError(() => of(true)) // Se a API cair, deixamos passar para não travar o cliente
    );
  }

  consultarCnpj(cnpj: string): Observable<any | null> {
    const cleanCnpj = cnpj.replace(/\D/g, '');
    return this.http.get<any>(`${this.URL}/cnpj/${cleanCnpj}?token=${this.TOKEN}`).pipe(
      catchError(() => of(null))
    );
  }

  validarEmail(email: string): Observable<boolean> {
    if (!email) return of(false);
    return this.http.get<any>(`${this.URL}/email-validator/${email}?token=${this.TOKEN}`).pipe(
      map(res => res.valid_format && !res.disposable),
      catchError(() => of(true)) // Tolerância a falhas
    );
  }
}
