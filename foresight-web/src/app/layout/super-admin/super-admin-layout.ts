import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';

@Component({
  selector: 'app-super-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './super-admin-layout.html',
  styleUrls: ['./super-admin-layout.scss']
})
export class SuperAdminLayoutComponent {
  private router = inject(Router);

  sair() {
    if (confirm('Deseja encerrar a sessão master?')) {
      localStorage.removeItem('auth_token');
      sessionStorage.clear();
      this.router.navigate(['/login']);
    }
  }
}
