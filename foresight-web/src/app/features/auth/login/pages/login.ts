import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loading = signal(false);
  errorMessage = signal<string | null>(null);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(6)]]
  });

  get f() {
    return this.loginForm.controls;
  }

  async realizarLogin() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    try {
      await this.authService.login(this.loginForm.getRawValue() as any);

      this.loading.set(false);

      if (this.authService.isSuperAdmin()) {
        console.log('[Security] Roteando para Ambiente Master');
        await this.router.navigateByUrl('/backoffice/dashboard', { replaceUrl: true });
      } else {
        console.log('[Security] Roteando para Ambiente Inquilino');
        await this.router.navigateByUrl('/sistema/dashboard', { replaceUrl: true });
      }
    } catch (err: any) {
      this.loading.set(false);
      const msg = err.error?.message || 'E-mail ou senha incorretos.';
      this.errorMessage.set(msg);
    }
  }
}
