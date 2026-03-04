import { Component, OnInit, inject, signal, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { AuditoriaService } from '../../services/auditoria.service';
import { AuditoriaLogDto } from '../../models/auditoria.model';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-auditoria-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './auditoria-admin.html',
  styleUrls: ['./auditoria-admin.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush // Alta performance
})
export class AuditoriaAdminComponent implements OnInit {
  private auditoriaService = inject(AuditoriaService);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  // Estados
  logs = signal<AuditoriaLogDto[]>([]);
  totalElementos = signal<number>(0);
  loading = signal<boolean>(true);
  paginaAtual = signal<number>(0);
  itensPorPagina = 50;

  // Formulário de Filtros Dinâmicos
  filtroForm: FormGroup = this.fb.group({
    termo: [''],
    acao: ['TODAS'],
    dataInicio: [''],
    dataFim: ['']
  });

  // Lista de ações para o select (pode ser mockada ou vinda de um Enum)
  acoesDisponiveis = [
    'MUDANCA_STATUS_EMPRESA',
    'LOGIN_FALHO',
    'ALTERACAO_PLANO',
    'EXCLUSAO_USUARIO',
    'ACESSO_IMPERSONATION'
  ];

  ngOnInit(): void {
    this.carregarLogs();
    this.escutarFiltros();
  }

  // Monitora alterações nos filtros e busca no backend com debounce (proteção anti-DDoS local)
  private escutarFiltros(): void {
    this.filtroForm.valueChanges.pipe(
      debounceTime(600),
      distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
      switchMap(valores => {
        this.loading.set(true);
        this.paginaAtual.set(0); // Volta para a pág 1 ao filtrar
        return this.auditoriaService.listarLogs(
          0,
          this.itensPorPagina,
          valores.termo,
          valores.acao,
          valores.dataInicio,
          valores.dataFim
        ).pipe(
          catchError(() => {
            this.loading.set(false);
            return of({ data: { content: [], totalElements: 0 } });
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((res: any) => {
      this.logs.set(res.data.content || []);
      this.totalElementos.set(res.data.totalElements || 0);
      this.loading.set(false);
    });
  }

  carregarLogs(): void {
    this.loading.set(true);
    const filtros = this.filtroForm.value;

    this.auditoriaService.listarLogs(
      this.paginaAtual(),
      this.itensPorPagina,
      filtros.termo,
      filtros.acao,
      filtros.dataInicio,
      filtros.dataFim
    ).subscribe({
      next: (res: any) => {
        this.logs.set(res.data.content || []);
        this.totalElementos.set(res.data.totalElements || 0);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        alert('Erro ao buscar logs de auditoria. Verifique suas permissões.');
      }
    });
  }

  mudarPagina(novaPagina: number): void {
    if (novaPagina < 0 || novaPagina >= Math.ceil(this.totalElementos() / this.itensPorPagina)) return;
    this.paginaAtual.set(novaPagina);
    this.carregarLogs(); // Chama direto sem passar pelo debounce
  }

  limparFiltros(): void {
    this.filtroForm.reset({ acao: 'TODAS', termo: '', dataInicio: '', dataFim: '' });
  }
}
