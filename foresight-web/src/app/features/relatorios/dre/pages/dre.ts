import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';
import { ApiResponse } from '../../../../core/http/api-response.model';

@Component({
  selector: 'app-dre',
  standalone: true,
  imports: [CommonModule, RouterModule, BrMaskPipe],
  templateUrl: './dre.html'
})
export class DreComponent implements OnInit {
  private http = inject(HttpClient);

  dre = signal<any>(null);
  loading = signal(true);

  private readonly API = 'http://localhost:8080/api/relatorios/dre';

  ngOnInit() {
    this.carregarDre();
  }

  carregarDre() {
    this.loading.set(true);
    this.http.get<ApiResponse<any>>(this.API).subscribe({
      next: (res) => {
        this.dre.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
