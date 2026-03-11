export interface ProdutoRequest {
  nome: string;
  categoria?: string | null;
  precoCusto: number;
  precoVenda: number;
  estoqueAtual: number;
  estoqueMinimo: number;
}

export interface ProdutoDto extends ProdutoRequest {
  id: number;
  lucroReal: number;
  margemReal: number;
  alertaStatus: string;
}
