import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DespesaService, DespesaRequest } from '../../services/despesa.service';
import { BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe'; // Pipe Import

@Component({
  selector: 'app-despesas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskPipe],
  templateUrl: './despesas.html',
  styleUrls: ['./despesas.scss']
})
export class DespesasComponent implements OnInit {
  private fb = inject(FormBuilder);
  private despesaService = inject(DespesaService);

  despesas = signal<any[]>([]);
  loading = signal(true);
  salvando = signal(false);
  erro = signal<string | null>(null);

  despesaForm: FormGroup = this.fb.group({
    descricao: ['', [Validators.required, Validators.maxLength(255)]],
    valor: ['', [Validators.required, Validators.min(0.01)]],
    data: ['', Validators.required],
    tipo: ['FIXA', Validators.required],
    ehPessoal: [false]
  });

  ngOnInit() {
    this.carregarDespesas();
  }

  carregarDespesas() {
    this.loading.set(true);
    this.despesaService.listar().subscribe({
      next: (res: any) => {
        this.despesas.set(res.data || []);
        this.loading.set(false);
      },
      error: () => {
        this.erro.set('Falha ao carregar o histórico de despesas.');
        this.loading.set(false);
      }
    });
  }

  lancarDespesa() {
    if (this.despesaForm.invalid) {
      this.despesaForm.markAllAsTouched();
      return;
    }

    this.salvando.set(true);
    this.erro.set(null);

    const formValues = this.despesaForm.value;

    let dataFormatada = formValues.data;
    if (dataFormatada && dataFormatada.length === 10) {
      dataFormatada = `${dataFormatada}T00:00:00`;
    }

    const payload: DespesaRequest = {
      descricao: formValues.descricao,
      valor: Number(formValues.valor),
      tipo: formValues.tipo,
      data: dataFormatada,
      ehPessoal: formValues.ehPessoal
    };

    this.despesaService.salvar(payload).subscribe({
      next: () => {
        this.despesaForm.reset({ tipo: 'FIXA', ehPessoal: false });
        this.salvando.set(false);
        this.carregarDespesas();
      },
      error: (err: any) => {
        this.erro.set(err.error?.message || 'Erro inesperado ao salvar despesa.');
        this.salvando.set(false);
      }
    });
  }

  excluir(id: number) {
    if (confirm('Tem certeza que deseja remover esta despesa? Isso não afetará o fluxo de caixa caso já esteja consolidado.')) {
      this.loading.set(true);
      this.despesaService.excluir(id).subscribe({
        next: () => this.carregarDespesas(),
        error: () => {
          this.erro.set('Erro ao excluir a despesa.');
          this.loading.set(false);
        }
      });
    }
  }

  get f() { return this.despesaForm.controls; }
}
