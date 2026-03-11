import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { RegistroRequest, TokenResponse } from '../../../../core/auth/models/auth.model';
import { ApiResponse } from '../../../../core/http/api-response.model';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './cadastro.html',
  styleUrls: ['./cadastro.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CadastroComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);

  loading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  cadastroForm: FormGroup = this.fb.group({
    nomeUsuario: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    telefoneUsuario: ['', [Validators.required, Validators.minLength(14)]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
    confirmarSenha: ['', [Validators.required]],
    nomeEmpresa: ['', [Validators.required, Validators.maxLength(150)]],
    cnpjEmpresa: ['', [Validators.required, Validators.minLength(18)]],
    tipoEmpresa: ['SERVICO', [Validators.required]]
  }, { validators: this.senhasIguaisValidator });

  get f() { return this.cadastroForm.controls; }

  private senhasIguaisValidator(control: AbstractControl): ValidationErrors | null {
    const senha = control.get('senha')?.value;
    const confirmarSenha = control.get('confirmarSenha')?.value;
    return senha === confirmarSenha ? null : { senhasDiferentes: true };
  }

  aplicarMascaraCNPJ(event: Event) {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, ''); // Remove tudo que não é dígito

    if (v.length > 14) v = v.substring(0, 14);

    v = v.replace(/^(\d{2})(\d)/, '$1.$2');
    v = v.replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3');
    v = v.replace(/\.(\d{3})(\d)/, '.$1/$2');
    v = v.replace(/(\d{4})(\d)/, '$1-$2');

    this.cadastroForm.patchValue({ cnpjEmpresa: v }, { emitEvent: false });
  }

  aplicarMascaraTelefone(event: Event) {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '');

    if (v.length > 11) v = v.substring(0, 11);

    if (v.length > 10) {
      v = v.replace(/^(\d{2})(\d{5})(\d{4}).*/, '($1) $2-$3');
    } else if (v.length > 6) {
      v = v.replace(/^(\d{2})(\d{4})(\d{0,4}).*/, '($1) $2-$3');
    } else if (v.length > 2) {
      v = v.replace(/^(\d{2})(\d{0,5})/, '($1) $2');
    } else if (v.length > 0) {
      v = v.replace(/^(\d{0,2})/, '($1');
    }

    this.cadastroForm.patchValue({ telefoneUsuario: v }, { emitEvent: false });
  }

  realizarCadastro() {
    this.errorMessage.set(null);

    if (this.cadastroForm.invalid) {
      this.errorMessage.set('Preencha todos os campos obrigatórios corretamente.');
      this.cadastroForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);

    const rawData = this.cadastroForm.value;
    const payload: RegistroRequest = {
      nomeUsuario: rawData.nomeUsuario,
      email: rawData.email,
      senha: rawData.senha,
      telefoneUsuario: rawData.telefoneUsuario.replace(/\D/g, ''),
      nomeEmpresa: rawData.nomeEmpresa,
      cnpjEmpresa: rawData.cnpjEmpresa.replace(/\D/g, ''),
      tipoEmpresa: rawData.tipoEmpresa
    };

    this.http.post<ApiResponse<TokenResponse>>('http://localhost:8080/api/auth/registrar', payload)
      .subscribe({
        next: (res) => {
          if(res.data?.token) {
            localStorage.setItem('auth_token', res.data.token);
            this.loading.set(false);
            this.router.navigate(['/sistema/dashboard']);
          }
        },
        error: (err: HttpErrorResponse) => {
          this.loading.set(false);
          const msg = err.error?.message || 'Erro de comunicação com o servidor.';
          this.errorMessage.set(msg);
        }
      });
  }
}
