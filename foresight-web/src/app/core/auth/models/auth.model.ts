export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegistroRequest {
  // Dados do Usuário
  nomeUsuario: string;
  email: string;
  senha: string;
  telefoneUsuario: string;

  // Dados do Tenant (Empresa)
  nomeEmpresa: string;
  cnpjEmpresa: string;
  tipoEmpresa: 'SERVICO' | 'PRODUTO' | 'SERVICO_PRODUTO';
}

export interface TokenResponse {
  token: string;
  nomeUsuario: string;
  nomeEmpresa: string;
}
