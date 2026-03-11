import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth-guard';
import { superAdminGuard } from './core/security/super-admin.guard';
import { PublicLayoutComponent } from './layout/public/public-layout';
import { PrivateLayoutComponent } from './layout/private/private-layout';
import { SuperAdminLayoutComponent } from './layout/super-admin/super-admin-layout';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login/pages/login').then(m => m.LoginComponent),
        title: 'Foresight - Acesso'
      },
      {
        path: 'cadastro',
        loadComponent: () => import('./features/auth/cadastro/pages/cadastro').then(m => m.CadastroComponent),
        title: 'Foresight - Criar Conta'
      },
      {
        path: 'recuperar-senha',
        loadComponent: () => import('./features/auth/recuperar-senha/pages/recuperar-senha').then(m => m.RecuperarSenhaComponent),
        title: 'Foresight - Recuperar Senha'
      }
    ]
  },

  {
    path: 'backoffice',
    component: SuperAdminLayoutComponent,
    canActivate: [superAdminGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/backoffice/pages/dashboard-admin/dashboard-admin').then(m => m.DashboardAdminComponent),
        title: 'Admin - Dashboard Global'
      },
      {
        path: 'empresas',
        loadComponent: () => import('./features/backoffice/pages/empresas-admin/empresas-admin').then(m => m.EmpresasAdminComponent),
        title: 'Admin - Gestão de Tenants'
      },
      {
        path: 'auditoria',
        loadComponent: () => import('./features/auditoria/pages/auditoria').then(m => m.AuditoriaComponent),
        title: 'Admin - Logs do Sistema'
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: '**', redirectTo: 'dashboard' }
    ]
  },

  {
    path: 'sistema',
    component: PrivateLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/pages/dashboard').then(m => m.DashboardComponent),
        title: 'Foresight - Dashboard'
      },
      {
        path: 'vendas',
        loadComponent: () => import('./features/vendas/pages/vendas').then(m => m.VendasComponent),
        title: 'Foresight - Vendas'
      },
      {
        path: 'vendas/historico',
        loadComponent: () => import('./features/vendas/pages/historico-vendas/historico-vendas').then(m => m.HistoricoVendasComponent)
      },
      {
        path: 'clientes',
        loadComponent: () => import('./features/clientes/pages/clientes').then(m => m.ClientesComponent),
        title: 'Foresight - Clientes'
      },
      {
        path: 'estoque',
        loadComponent: () => import('./features/produtos/pages/produtos').then(m => m.ProdutosComponent),
        title: 'Foresight - Estoque'
      },
      {
        path: 'estoque/analise',
        loadComponent: () => import('./features/produtos/pages/analise/inteligencia-preco').then(m => m.InteligenciaPrecoComponent)
      },
      {
        path: 'fluxo-caixa',
        loadComponent: () => import('./features/financeiro/pages/fluxo-caixa/fluxo-caixa').then(m => m.FluxoCaixaComponent),
        title: 'Foresight - Fluxo de Caixa'
      },
      {
        path: 'despesas',
        loadComponent: () => import('./features/financeiro/pages/despesas/despesas').then(m => m.DespesasComponent),
        title: 'Foresight - Despesas'
      },
      {
        path: 'relatorios/dre',
        loadComponent: () => import('./features/relatorios/dre/pages/dre').then(m => m.DreComponent),
        title: 'Foresight - DRE'
      },
      {
        path: 'relatorios/avancado',
        loadComponent: () => import('./features/relatorios/avancado/pages/relatorio-geral/relatorio-geral').then(m => m.RelatorioGeralComponent)
      },
      {
        path: 'configuracoes',
        loadComponent: () => import('./features/configuracoes/pages/configuracoes').then(m => m.ConfiguracoesComponent),
        title: 'Foresight - Configurações'
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: '**', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
