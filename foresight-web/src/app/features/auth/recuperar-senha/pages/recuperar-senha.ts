import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-recuperar-senha',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './recuperar-senha.html',
  // REMOVIDO: styles inline, pois agora usamos o CSS global do auth-wrapper
})
export class RecuperarSenhaComponent {
  private http = inject(HttpClient);
  private router = inject(Router);

  email = '';
  loading = signal(false);
  sucesso = signal(false);

  solicitarRecuperacao() {
    if (!this.email) return;

    this.loading.set(true);

    // Simulação de segurança (Delay para evitar enumeração de usuários por tempo de resposta)
    setTimeout(() => {
      this.sucesso.set(true);
      this.loading.set(false);
    }, 1500);

    // Futuro: this.http.post(...)
  }
}
