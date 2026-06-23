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

export interface FiltroRelatorio {
  contexto?: string; // NOVO: Para saber qual aba estamos
  termoBusca?: string | null;
  dataInicio?: string | null;
  dataFim?: string | null;
  tipo?: string | null;
  categoria?: string | null;
  page: number;
  size: number;
  sort?: string;
}
