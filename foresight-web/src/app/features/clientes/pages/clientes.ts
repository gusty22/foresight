import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ApiResponse } from '../../../core/http/api-response.model';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.html',
  styleUrl: './clientes.scss'
})
export class ClientesComponent implements OnInit {
  private http = inject(HttpClient);

  clientes = signal<any[]>([]);
  loading = signal(false);
  termoBusca = signal('');

  private readonly API = 'http://localhost:8080/api/clientes';

  ngOnInit() {
    this.carregarClientes();
  }

  carregarClientes() {
    this.loading.set(true);
    // Rota cega: O Backend usa o JWT para filtrar a empresa
    this.http.get<ApiResponse<any[]>>(this.API).subscribe({
      next: (res) => {
        this.clientes.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao listar clientes:', err);
        this.loading.set(false);
      }
    });
  }

  filtrarClientes() {
    const termo = this.termoBusca().toLowerCase();
    if(!termo) {
      this.carregarClientes();
      return;
    }
    // Filtro local para performance
    this.clientes.update(lista => lista.filter(c =>
      c.nome.toLowerCase().includes(termo) ||
      (c.documento && c.documento.includes(termo))
    ));
  }

  novoCliente() {
    alert('Funcionalidade de cadastro em desenvolvimento.');
  }
}
