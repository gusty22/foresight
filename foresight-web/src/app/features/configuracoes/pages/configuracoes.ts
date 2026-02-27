import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UsuarioService } from '../../usuarios/services/usuario.service';

@Component({
  selector: 'app-configuracoes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './configuracoes.html',
  styleUrls: ['./configuracoes.scss']
})
export class ConfiguracoesComponent implements OnInit {
  private fb = inject(FormBuilder);
  private usuarioService = inject(UsuarioService);

  abaAtiva = signal<'perfil' | 'empresa' | 'seguranca'>('perfil');
  loading = signal(true);
  salvando = signal(false);
  mensagemErro = signal<string | null>(null);
  mensagemSucesso = signal<string | null>(null);

  perfilAtual = signal<any>(null);

  perfilForm: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    email: [{ value: '', disabled: true }],
    telefone: ['', [Validators.required, Validators.minLength(14)]]
  });

  empresaForm: FormGroup = this.fb.group({
    nome: ['', Validators.required],
    cnpj: [{ value: '', disabled: true }], // CNPJ é imutável
    endereco: [''],
    proLaboreDesejado: [0],
    tipo: [{ value: 'SERVICO', disabled: true }]
  });

  senhaForm: FormGroup = this.fb.group({
    senhaAtual: ['', Validators.required],
    novaSenha: ['', [Validators.required, Validators.minLength(6)]],
    confirmarNovaSenha: ['', Validators.required]
  });

  ngOnInit() {
    this.carregarDados();
  }

  carregarDados() {
    this.loading.set(true);
    this.usuarioService.obterMeuPerfil().subscribe({
      next: (res: any) => {
        const dados = res.data ? res.data : res;
        this.perfilAtual.set(dados);

        // Preenche Perfil (Formatando telefone vindo do banco)
        this.perfilForm.patchValue({
          nome: dados.nome || '',
          email: dados.email || '',
          telefone: this.aplicarMascaraTelefoneString(dados.telefone || '')
        });

        // Preenche Empresa (Formatando CNPJ vindo do banco)
        if (dados.empresa) {
          this.empresaForm.patchValue({
            nome: dados.empresa.nome || '',
            cnpj: this.aplicarMascaraCNPJString(dados.empresa.cnpj || ''),
            endereco: dados.empresa.endereco || '',
            proLaboreDesejado: dados.empresa.proLaboreDesejado || 0,
            tipo: dados.empresa.tipo || 'SERVICO'
          });
        }
        this.loading.set(false);
      },
      error: () => {
        this.mensagemErro.set('Erro ao carregar dados do sistema.');
        this.loading.set(false);
      }
    });
  }

  // --- MÁSCARAS DE INTERAÇÃO (Eventos de Input) ---
  // Padronizado com o VendasComponent

  formatarTelefone(event: Event) {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, "");
    if (v.length > 11) v = v.slice(0, 11);
    if (v.length > 10) {
      v = v.replace(/^(\d\d)(\d{5})(\d{4}).*/, "($1) $2-$3");
    } else {
      v = v.replace(/^(\d\d)(\d{4})(\d{0,4}).*/, "($1) $2-$3");
    }
    input.value = v;
    this.perfilForm.get('telefone')?.setValue(v, { emitEvent: false });
  }

  // --- MÉTODOS AUXILIARES DE FORMATAÇÃO (Strings estáticas) ---

  private aplicarMascaraTelefoneString(v: string): string {
    v = v.replace(/\D/g, "");
    if (v.length > 10) {
      return v.replace(/^(\d\d)(\d{5})(\d{4}).*/, "($1) $2-$3");
    } else if (v.length > 2) {
      return v.replace(/^(\d\d)(\d{4})(\d{0,4}).*/, "($1) $2-$3");
    }
    return v;
  }

  private aplicarMascaraCNPJString(v: string): string {
    v = v.replace(/\D/g, '');
    if (v.length > 14) v = v.substring(0, 14);
    return v.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  }

  // --- AÇÕES ---

  mudarAba(aba: 'perfil' | 'empresa' | 'seguranca') {
    this.abaAtiva.set(aba);
    this.mensagemSucesso.set(null);
    this.mensagemErro.set(null);
  }

  salvarPerfil() {
    if (this.perfilForm.invalid) return;
    this.salvando.set(true);

    const payload = {
      nome: this.perfilForm.value.nome,
      // Sanitização: Remove a máscara antes de enviar
      telefone: this.perfilForm.value.telefone.replace(/\D/g, '')
    };

    this.usuarioService.atualizarMeuPerfil(payload).subscribe({
      next: () => this.exibirSucesso('Perfil atualizado com sucesso!'),
      error: () => this.exibirErro('Falha ao atualizar o perfil.')
    });
  }

  salvarEmpresa() {
    // Aqui seria a chamada ao service de empresa
    // Por enquanto simulamos o sucesso
    this.salvando.set(true);
    setTimeout(() => this.exibirSucesso('Dados organizacionais atualizados!'), 1000);
  }

  alterarSenha() {
    if (this.senhaForm.invalid) return;
    this.salvando.set(true);
    // Simulação segura
    setTimeout(() => {
      this.senhaForm.reset();
      this.exibirSucesso('Senha atualizada com segurança.');
    }, 1000);
  }

  private exibirSucesso(msg: string) {
    this.salvando.set(false);
    this.mensagemErro.set(null);
    this.mensagemSucesso.set(msg);
    setTimeout(() => this.mensagemSucesso.set(null), 3500);
  }

  private exibirErro(msg: string) {
    this.salvando.set(false);
    this.mensagemSucesso.set(null);
    this.mensagemErro.set(msg);
  }
}
