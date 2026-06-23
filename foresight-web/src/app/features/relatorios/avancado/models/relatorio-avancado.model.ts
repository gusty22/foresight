export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface TransacaoRelatorio {
  id: number;
  data: string;
  descricao: string;
  cliente: string;
  categoria: string;
  valor: number;
  tipo: string;
  status: string;
}

// NOVO DTO PARA O RANKING
export interface RankingVendas {
  produtoNome: string;
  quantidadeVendida: number;
  receitaTotal: number;
}

export interface FiltroRelatorio {
  contexto?: string;
  termoBusca?: string | null;
  dataInicio?: string | null;
  dataFim?: string | null;
  tipo?: string | null;
  categoria?: string | null;
  page: number;
  size: number;
  sort?: string;
}

export interface InadimplenciaRelatorio {
  vendaId: number;
  clienteNome: string;
  dataVencimento: string;
  diasAtraso: number;
  valorDevido: number;
  status: string;
}
