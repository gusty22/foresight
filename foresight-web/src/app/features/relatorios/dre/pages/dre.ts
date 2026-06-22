import { Component, OnInit, inject, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';
import { ApiResponse } from '../../../../core/http/api-response.model';

@Component({
  selector: 'app-dre',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './dre.html'
})
export class DreComponent implements OnInit {
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  dre = signal<any>(null);
  loading = signal(true);

  filtroForm: FormGroup;

  periodicidades = [
    { valor: 'MENSAL', nome: 'Mensal' },
    { valor: 'TRIMESTRAL', nome: 'Trimestral' },
    { valor: 'ANUAL', nome: 'Anual' }
  ];

  meses = [
    { valor: 1, nome: 'Janeiro' }, { valor: 2, nome: 'Fevereiro' },
    { valor: 3, nome: 'Março' }, { valor: 4, nome: 'Abril' },
    { valor: 5, nome: 'Maio' }, { valor: 6, nome: 'Junho' },
    { valor: 7, nome: 'Julho' }, { valor: 8, nome: 'Agosto' },
    { valor: 9, nome: 'Setembro' }, { valor: 10, nome: 'Outubro' },
    { valor: 11, nome: 'Novembro' }, { valor: 12, nome: 'Dezembro' }
  ];

  trimestres = [
    { valor: 1, nome: '1º Trimestre (Jan-Mar)' },
    { valor: 2, nome: '2º Trimestre (Abr-Jun)' },
    { valor: 3, nome: '3º Trimestre (Jul-Set)' },
    { valor: 4, nome: '4º Trimestre (Out-Dez)' }
  ];

  anos = [2023, 2024, 2025, 2026];

  private readonly API = 'http://localhost:8080/api/relatorios/dre';

  constructor() {
    const dataAtual = new Date();
    this.filtroForm = this.fb.group({
      periodicidade: ['MENSAL'],
      periodoValor: [dataAtual.getMonth() + 1],
      ano: [dataAtual.getFullYear()]
    });
  }

  ngOnInit() {
    this.escutarMudancaPeriodicidade();
    this.carregarDre();
  }

  private escutarMudancaPeriodicidade() {
    this.filtroForm.get('periodicidade')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(val => {
        if (val === 'MENSAL') {
          this.filtroForm.patchValue({ periodoValor: new Date().getMonth() + 1 });
        } else if (val === 'TRIMESTRAL') {
          this.filtroForm.patchValue({ periodoValor: 1 }); // Default Q1
        } else {
          this.filtroForm.patchValue({ periodoValor: null }); // Ano não precisa desse campo
        }
      });
  }

  carregarDre() {
    this.loading.set(true);

    const { periodicidade, periodoValor, ano } = this.filtroForm.value;
    let params = new HttpParams()
      .set('periodicidade', periodicidade)
      .set('ano', ano);

    if (periodoValor) {
      params = params.set('periodoValor', periodoValor);
    }

    this.http.get<ApiResponse<any>>(this.API, { params }).subscribe({
      next: (res) => {
        this.dre.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  exportarDados() {
    alert("Iniciando o download do DRE consolidado...");
  }
}
