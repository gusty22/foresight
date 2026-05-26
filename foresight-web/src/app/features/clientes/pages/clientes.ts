import { Component, OnInit, OnDestroy, inject, signal, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, AsyncValidatorFn } from '@angular/forms';
import { Subscription, of, Observable } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, switchMap, catchError, map } from 'rxjs/operators';
import { ClienteService } from '../services/cliente.service';
import { InvertextoService } from '../../../core/services/invertexto.service'; // Integração da API
import { ClienteDto, ClienteRequest } from '../models/cliente.model';
import { AppFormatter, BrMaskPipe } from '../../../shared/pipes/br-mask.pipe';
import { BrMaskDirective } from '../../../shared/directives/br-mask.directive';
import { BrValidators } from '../../../shared/validators/br-validators';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe, BrMaskDirective],
  templateUrl: './clientes.html',
  styleUrls: ['./clientes.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ClientesComponent implements OnInit, OnDestroy {
  private fb = inject(FormBuilder);
  private clienteService = inject(ClienteService);
  private invertextoService = inject(InvertextoService); // Injetando serviço do Invertexto

  listaOriginal = signal<ClienteDto[]>([]);
  loading = signal(false);
  salvando = signal(false);
  buscandoCep = signal(false);
  buscandoCnpj = signal(false); // NOVO: Controle de loading do CNPJ
  termoBusca = signal('');
  exibirModal = signal(false);
  clienteEmEdicaoId: number | null = null;
  mensagemErro = signal<string | null>(null);

  // Controle dinâmico da máscara de documento
  tipoDocumentoMask = signal<'cpf' | 'cnpj'>('cpf');

  private subs = new Subscription();

  // Formulário configurado com updateOn: 'blur' para otimizar chamadas de API
  clienteForm: FormGroup = this.fb.group({
    tipoCliente: ['PF', Validators.required],
    nome: ['', [Validators.required, Validators.maxLength(150), Validators.pattern(/^[a-zA-ZÀ-ÿ\s]+$/)]],
    documento: ['', [BrValidators.documento()], [this.validarDocumentoAsync()]], // Validador síncrono e assíncrono
    rgInscricaoEstadual: [''],
    dataNascimento: [''],
    email: ['', [Validators.email], [this.validarEmailAsync()]], // Validador síncrono e assíncrono
    telefone: ['', [Validators.required, Validators.minLength(14)]],
    telefoneAlternativo: [''],
    cep: ['', Validators.minLength(9)],
    logradouro: [''],
    numero: [''],
    complemento: [''],
    bairro: [''],
    cidade: [''],
    estado: ['', [Validators.maxLength(2), Validators.pattern(/^[a-zA-Z]+$/)]],
    statusCliente: ['ATIVO', Validators.required],
    observacoes: ['']
  }, { updateOn: 'blur' });

  get f() { return this.clienteForm.controls; }

  ngOnInit(): void {
    this.carregarClientes();
    this.configurarBuscaCepAutomatica();
    this.configurarInteligenciaFormulario(); // Inicializa listeners de formulário
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  private configurarInteligenciaFormulario(): void {
    // 1. Troca PF / PJ dinamicamente e altera a máscara
    this.subs.add(
      this.clienteForm.get('tipoCliente')?.valueChanges.subscribe(tipo => {
        this.tipoDocumentoMask.set(tipo === 'PF' ? 'cpf' : 'cnpj');
        this.clienteForm.get('documento')?.setValue(''); // Limpa o campo para evitar conflitos
      })
    );

    // 2. Autopreenchimento de CNPJ
    this.subs.add(
      this.clienteForm.get('documento')?.valueChanges.pipe(
        filter(() => this.clienteForm.get('tipoCliente')?.value === 'PJ'), // Apenas se for PJ
        map(doc => doc ? doc.replace(/\D/g, '') : ''),
        filter(doc => doc.length === 14), // Apenas executa quando os 14 números estiverem preenchidos
        distinctUntilChanged()
      ).subscribe(cnpj => this.buscarDadosCnpj(cnpj))
    );
  }

  private buscarDadosCnpj(cnpj: string): void {
    this.buscandoCnpj.set(true);
    this.invertextoService.consultarCnpj(cnpj).subscribe(dados => {
      this.buscandoCnpj.set(false);

      if (dados && dados.razao_social) {
        this.clienteForm.patchValue({
          nome: dados.razao_social,
          email: dados.email || '',
          telefone: AppFormatter.telefone(dados.telefone1 || ''),
          cep: AppFormatter.cep(dados.endereco?.cep || ''),
          logradouro: dados.endereco?.logradouro || '',
          numero: dados.endereco?.numero || '',
          complemento: dados.endereco?.complemento || '',
          bairro: dados.endereco?.bairro || '',
          cidade: dados.endereco?.municipio || '',
          estado: dados.endereco?.uf || '',
          rgInscricaoEstadual: dados.natureza_juridica_codigo || ''
        });
        this.mensagemErro.set(null);
      } else {
        this.mensagemErro.set('CNPJ não encontrado na base de dados da Receita Federal.');
      }
    });
  }

  private configurarBuscaCepAutomatica(): void {
    this.subs.add(
      this.clienteForm.get('cep')!.valueChanges
        .pipe(
          filter(valor => !!valor),
          debounceTime(500),
          distinctUntilChanged(),
          filter(valor => valor.replace(/\D/g, '').length === 8),
          switchMap(valor => {
            this.buscandoCep.set(true);
            return this.clienteService.buscarCep(valor).pipe(catchError(() => {
              this.buscandoCep.set(false);
              return of(null);
            }));
          })
        ).subscribe(dados => {
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
      })
    );
  }

  // --- VALIDATORS ASSÍNCRONOS (API Invertexto) ---

  validarDocumentoAsync(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      if (!control.value) return of(null);
      return this.invertextoService.validarDocumento(control.value).pipe(
        map(isValid => isValid ? null : { documentoInvalidoApi: true })
      );
    };
  }

  validarEmailAsync(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<{ [key: string]: any } | null> => {
      if (!control.value) return of(null);
      return this.invertextoService.validarEmail(control.value).pipe(
        map(isValid => isValid ? null : { emailInvalidoApi: true })
      );
    };
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
      error: () => this.loading.set(false)
    });
  }

  salvarCliente(): void {
    this.mensagemErro.set(null);

    if (this.clienteForm.invalid) {
      this.clienteForm.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    const formValue = this.clienteForm.getRawValue();

    const payload: ClienteRequest = {
      nome: formValue.nome,
      documento: formValue.documento ? formValue.documento.replace(/\D/g, '') : null,
      telefone: formValue.telefone ? formValue.telefone.replace(/\D/g, '') : null,
      telefoneAlternativo: formValue.telefoneAlternativo ? formValue.telefoneAlternativo.replace(/\D/g, '') : null,
      cep: formValue.cep ? formValue.cep.replace(/\D/g, '') : null,
      email: formValue.email || null,
      dataNascimento: formValue.dataNascimento ? formValue.dataNascimento : null,
      logradouro: formValue.logradouro,
      numero: formValue.numero,
      bairro: formValue.bairro,
      cidade: formValue.cidade,
      estado: formValue.estado ? formValue.estado.toUpperCase() : null,
      tipoCliente: formValue.tipoCliente,
      inscricaoEstadual: formValue.rgInscricaoEstadual || null,
      observacoes: formValue.observacoes,
      statusCliente: formValue.statusCliente
    };

    const request$ = this.clienteEmEdicaoId
      ? this.clienteService.atualizar(this.clienteEmEdicaoId, payload)
      : this.clienteService.criar(payload);

    request$.subscribe({
      next: () => this.finalizarSalvamento(),
      error: (err) => {
        this.salvando.set(false);
        const msg = err.error?.message || 'Erro de validação ao salvar cliente. Verifique os dados.';
        this.mensagemErro.set(msg);
      }
    });
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
    this.mensagemErro.set(null);
    this.clienteForm.reset({ tipoCliente: 'PF', statusCliente: 'ATIVO' });
    this.tipoDocumentoMask.set('cpf'); // Força máscara de CPF ao abrir modal novo
    this.exibirModal.set(true);
  }

  abrirModalEdicao(cliente: ClienteDto): void {
    this.clienteEmEdicaoId = cliente.id || null;
    this.mensagemErro.set(null);

    // Ajusta a máscara antes de inserir os dados para formatar corretamente
    this.tipoDocumentoMask.set(cliente.tipoCliente === 'PJ' ? 'cnpj' : 'cpf');

    this.clienteForm.patchValue({
      ...cliente,
      documento: AppFormatter.documento(cliente.documento || ''),
      telefone: AppFormatter.telefone(cliente.telefone || ''),
      telefoneAlternativo: AppFormatter.telefone(cliente.telefoneAlternativo || ''),
      cep: AppFormatter.cep(cliente.cep || ''),
      rgInscricaoEstadual: cliente.inscricaoEstadual
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
}
