import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './acerto-lucros.html'
})
export class AuditoriaComponent implements OnInit {
  private http = inject(HttpClient);

  logs = signal<any[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.carregarLogs();
  }

  carregarLogs() {
    this.loading.set(true);
    this.http.get<any[]>(`http://localhost:8080/api/auditoria`)
      .subscribe({
        next: (res) => {
          this.logs.set(res);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar auditoria', err);
          this.loading.set(false);
        }
      });
  }

  getBadgeClass(acao: string) {
    switch (acao) {
      case 'CRIACAO': return 'bg-success-subtle text-success';
      case 'EDICAO': return 'bg-warning-subtle text-warning';
      case 'DELECAO': return 'bg-danger-subtle text-danger';
      default: return 'bg-secondary-subtle text-secondary';
    }
  }
}
