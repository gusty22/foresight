import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.estaLogado()) {
    if (authService.isSuperAdmin()) {
      router.navigateByUrl('/backoffice/dashboard');
      return false;
    }
    return true;
  }

  authService.logout();
  return false;
};
