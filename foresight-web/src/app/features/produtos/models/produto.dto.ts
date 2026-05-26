export interface ProdutoRequest {
  nome: string;
  codigoBarras?: string | null;
  imagemUrl?: string | null;
  categoriaId?: number | null;
  fornecedorId?: number | null;
  precoCusto: number;
  precoVenda: number;
  estoqueAtual: number;
  estoqueMinimo: number;
  investidorId?: number | null;
  percentualLucroInvestidor?: number | null;
}

export interface ProdutoDto {
  id: number;
  nome: string;
  codigoBarras?: string | null;
  imagemUrl?: string | null;
  categoriaId?: number | null;
  categoriaNome?: string | null;
  fornecedorId?: number | null;
  fornecedorNome?: string | null;
  precoCusto: number;
  precoVenda: number;
  estoqueAtual: number;
  estoqueMinimo: number;
  lucroReal: number;
  margemReal: number;
  alertaStatus: string;
}
