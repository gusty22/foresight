import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ClienteService, ClienteDto } from '../services/cliente.service';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.html',
  styleUrls: ['./clientes.scss']
})
export class ClientesComponent implements OnInit {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);

  // ESTADO REATIVO
  listaOriginal = signal<ClienteDto[]>([]);
  loading = signal(false);
  salvando = signal(false);
  termoBusca = signal('');

  // CONTROLE DE MODAL
  exibirModal = signal(false);
  clienteEmEdicaoId: number | null = null;

  // FORMULÁRIO SEGURO (DTO Fechado)
  clienteForm: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    tipoCliente: ['PF', Validators.required],
    documento: [''], // Aplicaremos máscara no HTML
    telefone: [''],
    email: ['', [Validators.email]],
    cidade: [''],
    estado: [''],
    statusCliente: ['ATIVO', Validators.required]
  });

  get f() { return this.clienteForm.controls; }

  ngOnInit(): void {
    this.carregarClientes();
  }

  // --- FILTROS (Client-side performance) ---
  clientesFiltrados = computed(() => {
    const termo = this.termoBusca().toLowerCase();
    return this.listaOriginal().filter(c =>
      c.nome.toLowerCase().includes(termo) ||
      (c.documento && c.documento.includes(termo)) ||
      (c.telefone && c.telefone.includes(termo))
    );
  });

  // --- MÉTODOS HTTP ---
  carregarClientes(): void {
    this.loading.set(true);
    this.clienteService.listar().subscribe({
      next: (res) => {
        this.listaOriginal.set(res.data || []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar clientes', err);
        this.loading.set(false);
      }
    });
  }

  salvarCliente(): void {
    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      return;
    }

    this.salvando.set(true);

    // Sanitização básica antes do envio
    const payload: ClienteDto = {
      ...this.clienteForm.value,
      documento: this.clienteForm.value.documento?.replace(/\D/g, ''),
      telefone: this.clienteForm.value.telefone?.replace(/\D/g, '')
    };

    if (this.clienteEmEdicaoId) {
      this.clienteService.atualizar(this.clienteEmEdicaoId, payload).subscribe({
        next: () => this.finalizarSalvamento(),
        error: () => this.salvando.set(false)
      });
    } else {
      this.clienteService.criar(payload).subscribe({
        next: () => this.finalizarSalvamento(),
        error: () => this.salvando.set(false)
      });
    }
  }

  excluirCliente(id: number | undefined): void {
    if (!id) return;
    if (confirm('Atenção: Tem certeza que deseja excluir este cliente? Histórico de vendas poderá ser anonimizado.')) {
      this.loading.set(true);
      this.clienteService.excluir(id).subscribe({
        next: () => this.carregarClientes(),
        error: () => this.loading.set(false)
      });
    }
  }

  // --- CONTROLE DE UI ---
  abrirModalNovo(): void {
    this.clienteEmEdicaoId = null;
    this.clienteForm.reset({ tipoCliente: 'PF', statusCliente: 'ATIVO' });
    this.exibirModal.set(true);
  }

  abrirModalEdicao(cliente: ClienteDto): void {
    this.clienteEmEdicaoId = cliente.id || null;
    this.clienteForm.patchValue({
      nome: cliente.nome,
      tipoCliente: cliente.tipoCliente || 'PF',
      documento: this.mascararDocumentoString(cliente.documento || ''),
      telefone: this.mascararTelefoneString(cliente.telefone || ''),
      email: cliente.email,
      cidade: cliente.cidade,
      estado: cliente.estado,
      statusCliente: cliente.statusCliente || 'ATIVO'
    });
    this.exibirModal.set(true);
  }

  fecharModal(): void {
    this.exibirModal.set(false);
    this.clienteEmEdicaoId = null;
    this.clienteForm.reset();
  }

  private finalizarSalvamento(): void {
    this.fecharModal();
    this.salvando.set(false);
    this.carregarClientes();
  }


  // --- EVENTOS DE MÁSCARA (Inputs) ---
  formatarDocumento(event: Event): void {
    const input = event.target as HTMLInputElement;
    const tipo = this.clienteForm.get('tipoCliente')?.value;
    let v = input.value.replace(/\D/g, "");

    if (tipo === 'PF') {
      if (v.length > 11) v = v.slice(0, 11);
      v = v.replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");
    } else {
      if (v.length > 14) v = v.slice(0, 14);
      v = v.replace(/^(\d{2})(\d)/, "$1.$2").replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3").replace(/\.(\d{3})(\d)/, ".$1/$2").replace(/(\d{4})(\d)/, "$1-$2");
    }
    input.value = v;
    this.clienteForm.get('documento')?.setValue(v, { emitEvent: false });
  }

  formatarTelefone(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, "");
    if (v.length > 11) v = v.slice(0, 11);
    if (v.length > 10) v = v.replace(/^(\d\d)(\d{5})(\d{4}).*/, "($1) $2-$3");
    else v = v.replace(/^(\d\d)(\d{4})(\d{0,4}).*/, "($1) $2-$3");

    input.value = v;
    this.clienteForm.get('telefone')?.setValue(v, { emitEvent: false });
  }

  // Helpers para edição
  private mascararDocumentoString(v: string): string {
    if (!v) return '';
    if (v.length === 11) return v.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
    if (v.length === 14) return v.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
    return v;
  }

  private mascararTelefoneString(v: string): string {
    if (!v) return '';
    if (v.length === 11) return v.replace(/^(\d{2})(\d{5})(\d{4})/, "($1) $2-$3");
    if (v.length === 10) return v.replace(/^(\d{2})(\d{4})(\d{4})/, "($1) $2-$3");
    return v;
  }
}
