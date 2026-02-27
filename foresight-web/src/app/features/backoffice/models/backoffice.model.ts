export type StatusEmpresa = 'ATIVA' | 'SUSPENSA' | 'CANCELADA' | 'INADIMPLENTE';

export interface EmpresaGlobalDto {
  id: number;
  razaoSocial: string;
  cnpj: string;
  status: StatusEmpresa;
  dataCadastro: string;
}

export interface AlterarStatusEmpresaRequest {
  novoStatus: StatusEmpresa;
  motivo?: string;
}
