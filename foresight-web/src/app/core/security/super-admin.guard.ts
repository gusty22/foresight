import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const superAdminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('auth_token');

  if (!token) {
    console.warn('[Guard] Acesso negado: Token ausente.');
    router.navigate(['/login']);
    return false;
  }

  try {
    const payload = JSON.parse(atob(token.split('.')[1]));

    // Alinhamento com o Backend: O Swagger mostrou que a chave é 'role'
    const role = payload.role || '';
    const isSuperAdmin = role === 'ROLE_SUPER_ADMIN';

    if (!isSuperAdmin) {
      console.error('[Guard] Acesso negado: Usuário não possui privilégios ROLE_SUPER_ADMIN. Role encontrada:', role);
      router.navigate(['/sistema/dashboard']);
      return false;
    }

    console.log('[Guard] Acesso concedido ao Backoffice.');
    return true;
  } catch (e) {
    console.error('[Guard] Erro ao decodificar token para validação de rota.');
    localStorage.removeItem('auth_token');
    router.navigate(['/login']);
    return false;
  }
};
