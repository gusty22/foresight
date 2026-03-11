import { Pipe, PipeTransform } from '@angular/core';
import { DatePipe } from '@angular/common';

export class AppFormatter {
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

  static moeda(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') return 'R$ 0,00';
    const num = typeof valor === 'string' ? parseFloat(valor) : valor;
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(num);
  }

  static percentual(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined || valor === '') return '0%';
    const num = typeof valor === 'string' ? parseFloat(valor) : valor;
    return new Intl.NumberFormat('pt-BR', { style: 'percent', minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num);
  }

  static decimal(valor: number | string | null | undefined): string {
    if (valor === null || valor === undefined) return '0';
    const num = typeof valor === 'string' ? parseFloat(valor) : valor;
    return new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num);
  }

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

export type BrFormatType = 'cpf' | 'cnpj' | 'doc' | 'tel' | 'cep' | 'moeda' | 'percent' | 'decimal' | 'data' | 'dataHora';

@Pipe({
  name: 'brFmt',
  standalone: true
})
export class BrMaskPipe implements PipeTransform {

  transform(value: any, tipo: BrFormatType): string {
    if (value === null || value === undefined || value === '') return '';

    switch (tipo) {
      case 'doc': return AppFormatter.documento(String(value));
      case 'cpf': return AppFormatter.cpf(String(value));
      case 'cnpj': return AppFormatter.cnpj(String(value));
      case 'tel': return AppFormatter.telefone(String(value));
      case 'cep': return AppFormatter.cep(String(value));
      case 'moeda': return AppFormatter.moeda(value);
      case 'percent': return AppFormatter.percentual(value);
      case 'decimal': return AppFormatter.decimal(value);
      case 'data': return AppFormatter.data(value);
      case 'dataHora': return AppFormatter.dataHora(value);

      default: return String(value);
    }
  }
}
