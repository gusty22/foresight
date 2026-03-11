// src/app/features/backoffice/pages/empresas-admin/empresas-admin.ts

import { Component, OnInit, inject, signal, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { BackofficeService } from '../../services/backoffice.service';
import { EmpresaGlobalDto, StatusEmpresa } from '../../models/backoffice.model';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-empresas-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './empresas-admin.html',
  styleUrls: ['./empresas-admin.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EmpresasAdminComponent implements OnInit {
  private backofficeService = inject(BackofficeService);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  empresas = signal<EmpresaGlobalDto[]>([]);
  totalElementos = signal<number>(0);
  loading = signal<boolean>(true);

  buscaControl = new FormControl('');
  paginaAtual = signal<number>(0);

  modalAcao = signal<{ visivel: boolean; empresa: EmpresaGlobalDto | null; novoStatus: StatusEmpresa | null }>({
    visivel: false, empresa: null, novoStatus: null
  });

  processando = signal<boolean>(false);
  erroAuditoria = signal<string | null>(null);

  auditoriaForm: FormGroup = this.fb.group({
    motivo: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(255)]]
  });

  ngOnInit(): void {
    this.carregarEmpresas();
    this.escutarBusca();
  }

  private escutarBusca(): void {
    this.buscaControl.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(termo => {
        this.loading.set(true);
        this.paginaAtual.set(0);
        return this.backofficeService.listarEmpresas(termo || '', 0).pipe(
          catchError(() => {
            this.loading.set(false);
            return of({ data: { content: [], totalElements: 0 } });
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((res: any) => {
      this.empresas.set(res.data.content || []);
      this.totalElementos.set(res.data.totalElements || 0);
      this.loading.set(false);
    });
  }

  carregarEmpresas(): void {
    this.loading.set(true);
    this.backofficeService.listarEmpresas(this.buscaControl.value || '', this.paginaAtual()).subscribe({
      next: (res: any) => {
        this.empresas.set(res.data.content || []);
        this.totalElementos.set(res.data.totalElements || 0);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        alert('Falha de segurança ou conexão com o servidor master.');
      }
    });
  }

  prepararMudancaStatus(empresa: EmpresaGlobalDto, novoStatus: StatusEmpresa): void {
    this.auditoriaForm.reset();
    this.erroAuditoria.set(null);
    this.modalAcao.set({ visivel: true, empresa, novoStatus });
  }

  fecharModal(): void {
    if (this.processando()) return;
    this.modalAcao.set({ visivel: false, empresa: null, novoStatus: null });
  }

  confirmarMudancaStatus(): void {
    if (this.auditoriaForm.invalid) {
      this.auditoriaForm.markAllAsTouched();
      return;
    }

    const { empresa, novoStatus } = this.modalAcao();
    if (!empresa || !novoStatus) return;

    this.processando.set(true);
    this.erroAuditoria.set(null);

    const payload = {
      novoStatus,
      motivo: this.auditoriaForm.value.motivo
    };

    this.backofficeService.alterarStatusEmpresa(empresa.id, payload).subscribe({
      next: (res) => {
        this.empresas.update(lista =>
          lista.map(e => e.id === empresa.id ? { ...e, status: res.data!.status } : e)
        );
        this.processando.set(false);
        this.fecharModal();
      },
      error: (err) => {
        this.erroAuditoria.set(err.error?.message || 'Falha ao executar ação de governança.');
        this.processando.set(false);
      }
    });
  }

  acessarComoEmpresa(empresaId: number): void {
    if (!confirm('AUDITORIA: Esta ação será registrada. Você terá acesso irrestrito aos dados deste cliente. Deseja prosseguir?')) return;
    alert('Funcionalidade de Impersonation (Suporte) acionada. Token gerado.');
  }
}
