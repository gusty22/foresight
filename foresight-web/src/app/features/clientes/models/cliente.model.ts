export type TipoCliente = 'PF' | 'PJ';
export type StatusCliente = 'ATIVO' | 'INATIVO' | 'INADIMPLENTE';

export interface ClienteDto {
  id?: number;
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
  tipoCliente?: TipoCliente;
  inscricaoEstadual?: string;
  condicoesEspeciais?: string;
  observacoes?: string;
  statusCliente?: StatusCliente;
}

export interface ClienteRequest extends Omit<ClienteDto, 'id'> {}

export interface ViaCepResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  ibge: string;
  gia: string;
  ddd: string;
  siafi: string;
  erro?: boolean;
}
