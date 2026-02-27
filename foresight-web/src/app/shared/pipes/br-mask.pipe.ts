import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

// ============================================================================
// 1. CLASSE UTILITÁRIA ESTÁTICA (Para usar no TypeScript)
// ============================================================================
export class AppFormatter {

  // --- MÁSCARAS DE DOCUMENTOS ---

  static documento(v: string | null | undefined): string {
    if (!v) return '';
    const s = v.replace(/\D/g, '');
    return s.length <= 11 ? AppFormatter.cpf(s) : AppFormatter.cnpj(s);
  }

  static cpf(v: string): string {
    v = v.replace(/\D/g, '');
    return v.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }

  static cnpj(v: string): string {
    v = v.replace(/\D/g, '');
    return v.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  }

  static telefone(v: string | null | undefined): string {
    if (!v) return '';
    let s = v.replace(/\D/g, '');
    if (s.length === 11) {
      return s.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }
    return s.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  }

  static cep(v: string | null | undefined): string {
    if (!v) return '';
    let s = v.replace(/\D/g, '');
    return s.replace(/^(\d{5})(\d{3})/, '$1-$2');
  }

  // --- FORMATAÇÃO FINANCEIRA E NUMÉRICA (Intl API) ---

  static moeda(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') return 'R$ 0,00';
    const num = typeof valor === 'string' ? parseFloat(valor) : valor;
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(num);
  }

  static percentual(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') return '0%';
    const num = typeof valor === 'string' ? parseFloat(valor) : valor;
    // Assume que 0.1 = 10%. Se o banco mandar 10 para 10%, dividir por 100 antes.
    // Aqui assumimos padrão decimal (0.5 = 50%)
    return new Intl.NumberFormat('pt-BR', { style: 'percent', minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num);
  }

  static decimal(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined) return '0';
    const num = typeof valor === 'string' ? parseFloat(valor) : valor;
    return new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num);
  }

  // --- DATAS (Padrão Sistema) ---

  static dataHora(data: string | Date | null | undefined): string {
    if (!data) return '';
    const datePipe = new DatePipe('pt-BR');
    return datePipe.transform(data, 'dd/MM/yyyy HH:mm') || '';
  }

  static data(data: string | Date | null | undefined): string {
    if (!data) return '';
    const datePipe = new DatePipe('pt-BR');
    return datePipe.transform(data, 'dd/MM/yyyy') || '';
  }
}

// ============================================================================
// 2. O PIPE ANGULAR (Para usar no HTML)
// ============================================================================
// Tipos aceitos pelo Pipe
export type BrFormatType = 'cpf' | 'cnpj' | 'doc' | 'tel' | 'cep' | 'moeda' | 'percent' | 'decimal' | 'data' | 'dataHora';

@Pipe({
  name: 'brFmt', // Nome curto e fácil: 'brFmt'
  standalone: true
})
export class BrMaskPipe implements PipeTransform {

  transform(value: any, tipo: BrFormatType): string {
    if (value === null || value === undefined || value === '') return '';

    switch (tipo) {
      // Documentos
      case 'doc': return AppFormatter.documento(String(value));
      case 'cpf': return AppFormatter.cpf(String(value));
      case 'cnpj': return AppFormatter.cnpj(String(value));
      case 'tel': return AppFormatter.telefone(String(value));
      case 'cep': return AppFormatter.cep(String(value));

      // Financeiro
      case 'moeda': return AppFormatter.moeda(value);
      case 'percent': return AppFormatter.percentual(value);
      case 'decimal': return AppFormatter.decimal(value);

      // Datas
      case 'data': return AppFormatter.data(value);
      case 'dataHora': return AppFormatter.dataHora(value);

      default: return String(value);
    }
  }
}
