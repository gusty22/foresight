export type TipoEmpresa = 'SERVICO' | 'PRODUTO' | 'SERVICO_PRODUTO';

export interface EmpresaRequest {
  nome: string;
  cnpj?: string;
  email?: string;
  telefone?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  tipo: TipoEmpresa;
  proLaboreDesejado: number;
}

export interface EmpresaDto extends EmpresaRequest {
  id: number;
}
