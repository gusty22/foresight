// src/app/features/backoffice/models/backoffice.model.ts

export type StatusEmpresa = 'ATIVA' | 'SUSPENSA' | 'CANCELADA' | 'INADIMPLENTE';
export type PlanoEmpresa = 'START' | 'PRO' | 'ENTERPRISE';

export interface EmpresaGlobalDto {
  id: number;
  razaoSocial: string;
  cnpj: string;
  status: StatusEmpresa;
  plano: PlanoEmpresa;
  dataCadastro: string;
  usuariosAtivos: number;
}

export interface AlterarStatusEmpresaRequest {
  novoStatus: StatusEmpresa;
  motivo: string; // Agora obrigatório por segurança
}

export interface DashboardGlobalDto {
  totalEmpresas: number;
  empresasAtivas: number;
  empresasSuspensas: number;
  faturamentoMensalEstimado: number;
  totalUsuariosGlobais: number;
}
