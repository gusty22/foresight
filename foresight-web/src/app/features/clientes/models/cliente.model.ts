export interface Cliente {
  id: number;
  nome: string;
  documento?: string;
  telefone?: string;
  telefoneAlternativo?: string;
  email?: string;
  dataNascimento?: string; // ISO string
  cep?: string;
  logradouro?: string;
  numero?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  tipoCliente?: string;
  inscricaoEstadual?: string;
  condicoesEspeciais?: string;
  observacoes?: string;
  statusCliente?: string;
}

export interface ClienteRequest {
  nome: string;
  documento?: string;
  telefone?: string;
  telefoneAlternativo?: string;
  email?: string;
  dataNascimento?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  tipoCliente?: string;
  inscricaoEstadual?: string;
  condicoesEspeciais?: string;
  observacoes?: string;
  statusCliente?: string;
}
