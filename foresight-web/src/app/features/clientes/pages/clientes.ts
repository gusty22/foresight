import { Component, OnInit, OnDestroy, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, switchMap, catchError } from 'rxjs/operators';
import { ClienteService } from '../services/cliente.service';
import { ClienteDto, ClienteRequest } from '../models/cliente.model';
import { AppFormatter, BrMaskPipe } from '../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './clientes.html',
  styleUrls: ['./clientes.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClientesComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);

  listaOriginal = signal<ClienteDto[]>([]);
  loading = signal(false);
  salvando = signal(false);
  buscandoCep = signal(false);
  termoBusca = signal('');
  exibirModal = signal(false);
  clienteEmEdicaoId: number | null = null;

  private cepSubscription!: Subscription;

  clienteForm: FormGroup = this.fb.group({
    tipoCliente: ['PF', Validators.required],
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    documento: [''],
    rgInscricaoEstadual: [''],
    dataNascimento: [''],
    email: ['', [Validators.email]],
    telefone: ['', Validators.required],
    telefoneAlternativo: [''],
    cep: [''],
    logradouro: [''],
    numero: [''],
    complemento: [''],
    bairro: [''],
    cidade: [''],
    estado: ['', Validators.maxLength(2)],
    statusCliente: ['ATIVO', Validators.required],
    observacoes: ['']
  });

  get f() { return this.clienteForm.controls; }

  ngOnInit(): void {
    this.carregarClientes();
    this.configurarBuscaCepAutomatica();
  }

  ngOnDestroy(): void {
    // Previne Memory Leak
    if (this.cepSubscription) {
      this.cepSubscription.unsubscribe();
    }
  }

  private configurarBuscaCepAutomatica(): void {
    this.cepSubscription = this.clienteForm.get('cep')!.valueChanges
      .pipe(
        filter(valor => !!valor),
        debounceTime(500),
        distinctUntilChanged(),
        filter(valor => valor.replace(/\D/g, '').length === 8),
        switchMap(valor => {
          this.buscandoCep.set(true);
          return this.clienteService.buscarCep(valor).pipe(
            catchError(() => {
              this.buscandoCep.set(false);
              return of(null);
            })
          );
        })
      )
      .subscribe(dados => {
        this.buscandoCep.set(false);
        if (dados && !dados.erro) {
          this.clienteForm.patchValue({
            logradouro: dados.logradouro,
            bairro: dados.bairro,
            cidade: dados.localidade,
            estado: dados.uf
          });
          document.getElementById('numeroInput')?.focus();
        }
      });
  }

  clientesFiltrados = computed(() => {
    const termo = this.termoBusca().toLowerCase();
    return this.listaOriginal().filter(c =>
      c.nome.toLowerCase().includes(termo) ||
      (c.documento && c.documento.includes(termo)) ||
      (c.telefone && c.telefone.includes(termo))
    );
  });

  carregarClientes(): void {
    this.loading.set(true);
    this.clienteService.listar().subscribe({
      next: (res) => {
        this.listaOriginal.set(res.data || []);
        this.loading.set(false);
      },
      error: () => {
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
    const formValue = this.clienteForm.getRawValue();
    const payload: ClienteRequest = {
      ...formValue,
      documento: formValue.documento ? formValue.documento.replace(/\D/g, '') : null,
      telefone: formValue.telefone ? formValue.telefone.replace(/\D/g, '') : null,
      telefoneAlternativo: formValue.telefoneAlternativo ? formValue.telefoneAlternativo.replace(/\D/g, '') : null,
      cep: formValue.cep ? formValue.cep.replace(/\D/g, '') : null
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
    if (confirm('Atenção: Tem certeza que deseja excluir este cliente?')) {
      this.loading.set(true);
      this.clienteService.excluir(id).subscribe({
        next: () => this.carregarClientes(),
        error: () => this.loading.set(false)
      });
    }
  }

  abrirModalNovo(): void {
    this.clienteEmEdicaoId = null;
    this.clienteForm.reset({ tipoCliente: 'PF', statusCliente: 'ATIVO' });
    this.exibirModal.set(true);
  }

  abrirModalEdicao(cliente: ClienteDto): void {
    this.clienteEmEdicaoId = cliente.id || null;
    this.clienteForm.patchValue({
      ...cliente,
      documento: AppFormatter.documento(cliente.documento || ''),
      telefone: AppFormatter.telefone(cliente.telefone || ''),
      telefoneAlternativo: AppFormatter.telefone(cliente.telefoneAlternativo || ''),
      cep: AppFormatter.cep(cliente.cep || '')
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

  aplicarMascaraDocumento(event: Event): void {
    const input = event.target as HTMLInputElement;
    const formatado = AppFormatter.documento(input.value);
    this.clienteForm.get('documento')?.setValue(formatado, { emitEvent: false });
  }

  aplicarMascaraTelefone(event: Event, controle: string): void {
    const input = event.target as HTMLInputElement;
    const formatado = AppFormatter.telefone(input.value);
    this.clienteForm.get(controle)?.setValue(formatado, { emitEvent: false });
  }

  aplicarMascaraCep(event: Event): void {
    const input = event.target as HTMLInputElement;
    const formatado = AppFormatter.cep(input.value);
    this.clienteForm.get('cep')?.setValue(formatado);
  }
}
