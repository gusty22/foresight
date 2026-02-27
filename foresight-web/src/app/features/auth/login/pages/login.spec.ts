import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  constructor(private authService: AuthService, private router: Router) {}

  handleLogin(event: Event) {
    event.preventDefault();
    const form = event.target as HTMLFormElement;
    const email = (form.elements.namedItem('email') as HTMLInputElement).value;
    const senha = (form.elements.namedItem('password') as HTMLInputElement).value;

    this.authService.login({ email, senha }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => alert('Credenciais inválidas ou erro no servidor!')
    });
  }
}
