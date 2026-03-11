export interface MovimentacaoRecente {
  descricao: string;
  categoria: string;
  data: string;
  valor: number;
  tipo: 'ENTRADA' | 'SAIDA';
}

export interface DashboardResumo {
  faturamentoMes: number;
  lucroLiquido: number;
  novosClientes: number;
  estoqueCritico: number;
  saldoCaixa: number;
  metaSobrevivencia: number;
  percentualMeta: number;
  movimentacoesRecentes: MovimentacaoRecente[];
}
