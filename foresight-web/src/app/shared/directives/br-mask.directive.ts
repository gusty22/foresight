import { Directive, ElementRef, HostListener, Input, inject } from '@angular/core';
import { NgControl } from '@angular/forms';
import { AppFormatter } from '../pipes/br-mask.pipe';

@Directive({
  selector: '[brMask]',
  standalone: true
})
export class BrMaskDirective {
  @Input('brMask') tipo!: 'cpf' | 'cnpj' | 'tel' | 'cep' | 'rg';
  private el = inject(ElementRef);
  private control = inject(NgControl, { optional: true });

  @HostListener('input', ['$event'])
  onInput(event: any) {
    let val = event.target.value;
    if (!val) return;

    // Remove tudo que não for número (bloqueia letras instantaneamente)
    val = val.replace(/\D/g, '');
    let formatted = val;

    switch (this.tipo) {
      case 'cpf':
        val = val.substring(0, 11);
        formatted = AppFormatter.cpf(val);
        break;
      case 'cnpj':
        val = val.substring(0, 14);
        formatted = AppFormatter.cnpj(val);
        break;
      case 'tel':
        val = val.substring(0, 11);
        formatted = AppFormatter.telefone(val);
        break;
      case 'cep':
        val = val.substring(0, 8);
        formatted = AppFormatter.cep(val);
        break;
      case 'rg':
        val = val.substring(0, 14); // Limita RG a 14 números
        formatted = val;
        break;
    }

    this.el.nativeElement.value = formatted;
    if (this.control && this.control.control) {
      this.control.control.setValue(formatted, { emitEvent: false });
    }
  }
}
