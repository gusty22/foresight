export interface MovimentacaoRecente {
  descricao: string;
  categoria: string;
  data: string; // ou Date, se você fizer parsing
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
