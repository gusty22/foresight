import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BackofficeService } from '../../services/backoffice.service';
import { EmpresaGlobalDto, StatusEmpresa } from '../../models/backoffice.model';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-empresas-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, BrMaskPipe],
  templateUrl: './empresas-admin.html',
  styleUrls: ['./empresas-admin.scss']
})
export class EmpresasAdminComponent implements OnInit {
  private backofficeService = inject(BackofficeService);

  empresas = signal<EmpresaGlobalDto[]>([]);
  loading = signal<boolean>(true);
  processandoId = signal<number | null>(null);
  termoBusca = signal<string>('');

  totalEmpresas = computed(() => this.empresas().length);
  ativas = computed(() => this.empresas().filter(e => e.status === 'ATIVA').length);
  suspensas = computed(() => this.empresas().filter(e => e.status === 'SUSPENSA' || e.status === 'INADIMPLENTE').length);

  empresasFiltradas = computed(() => {
    const termo = this.termoBusca().toLowerCase();
    if (!termo) return this.empresas();
    return this.empresas().filter(e =>
      e.razaoSocial.toLowerCase().includes(termo) ||
      e.cnpj.includes(termo)
    );
  });

  ngOnInit(): void {
    this.carregarEmpresas();
  }

  carregarEmpresas(): void {
    this.loading.set(true);
    this.backofficeService.listarTodasEmpresas().subscribe({
      next: (res) => {
        this.empresas.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Falha de segurança ou conexão:', err);
        alert('Você não tem permissão para acessar esta área ou o servidor está fora.');
        this.loading.set(false);
      }
    });
  }

  mudarStatus(empresa: EmpresaGlobalDto, novoStatus: StatusEmpresa): void {
    if (empresa.status === novoStatus) return;

    const acao = novoStatus === 'SUSPENSA' ? 'SUSPENDER (Bloquear acesso)' : 'ATIVAR';
    if (!confirm(`ATENÇÃO: Deseja realmente ${acao} a empresa ${empresa.razaoSocial}?`)) {
      return;
    }

    const motivo = prompt('Motivo da alteração (Auditoria):', 'Ação administrativa manual');
    if (motivo === null) return;

    this.processandoId.set(empresa.id);

    this.backofficeService.alterarStatusEmpresa(empresa.id, { novoStatus, motivo }).subscribe({
      next: (res) => {
        this.empresas.update(lista =>
          lista.map(e => e.id === empresa.id ? { ...e, status: res.data!.status } : e)
        );
        this.processandoId.set(null);
      },
      error: (err) => {
        alert('Erro ao atualizar status: ' + (err.error?.message || 'Erro desconhecido.'));
        this.processandoId.set(null);
      }
    });
  }
}