export interface AuditoriaLogDto {
  id: number;
  empresaId: number | null;
  usuarioEmail: string;
  acao: string;
  detalhes: string;
  ipAddress: string;
  dataHora: string;
}
