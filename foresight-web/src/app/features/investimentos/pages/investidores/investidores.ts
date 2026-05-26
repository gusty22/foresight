import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InvestimentoService } from '../../services/investimento.service';
import { BrMaskDirective } from '../../../../shared/directives/br-mask.directive';
import { AppFormatter, BrMaskPipe } from '../../../../shared/pipes/br-mask.pipe';

@Component({
  selector: 'app-investidores',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, BrMaskDirective, BrMaskPipe], // Reactive e Diretiva Adicionados
  templateUrl: './investidores.html'
})
export class InvestidoresComponent implements OnInit {
  private service = inject(InvestimentoService);
  private fb = inject(FormBuilder);

  investidores = signal<any[]>([]);
  exibirRelatorio = signal(false);
  dadosRelatorio = signal<any>(null);
  loadingRelatorio = signal(false);
  investidorEmEdicaoId: number | null = null;
  loading = signal(false);
  // Adicione junto das variáveis lá em cima:
  exibirInputPagamento = signal(false);
  valorAPagarStr = signal('');
  valorAPagarNum = signal(0);
  processandoPagamento = signal(false);

  // E adicione estes métodos no final da classe:
  abrirInputPagamento(valorSugerido: number) {
    this.exibirInputPagamento.set(true);
    // Sugere o valor total por padrão
    this.valorAPagarNum.set(valorSugerido);
    this.valorAPagarStr.set(AppFormatter.decimal(valorSugerido));
  }

  cancelarPagamento() {
    this.exibirInputPagamento.set(false);
    this.valorAPagarStr.set('');
    this.valorAPagarNum.set(0);
  }

  formatarInputPagamento(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.value) {
      this.valorAPagarStr.set('');
      this.valorAPagarNum.set(0);
      return;
    }

    let valorStr = input.value.replace(/\D/g, '');
    if (!valorStr) valorStr = '0';

    const numerico = Number(valorStr) / 100;
    this.valorAPagarStr.set(AppFormatter.decimal(numerico));
    this.valorAPagarNum.set(numerico);
  }

  confirmarPagamentoParcial() {
    const investidor = this.dadosRelatorio()?.investidor;
    const valor = this.valorAPagarNum();

    if (!investidor || valor <= 0) {
      alert('Digite um valor válido maior que zero.');
      return;
    }

    if (valor > this.dadosRelatorio().totalRepassePendente) {
      alert('O valor não pode ser maior que o saldo pendente.');
      return;
    }

    this.processandoPagamento.set(true);
    this.service.pagarInvestidor(investidor.id, valor).subscribe({
      next: () => {
        this.processandoPagamento.set(false);
        this.cancelarPagamento();
        // Recarrega o relatório para atualizar os cards na hora!
        this.abrirFichaFinanceira(investidor.id);
      },
      error: (err) => {
        alert(err.error?.message || 'Erro ao registrar pagamento.');
        this.processandoPagamento.set(false);
      }
    });
  }

  investidorForm: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    telefone: ['', [Validators.required, Validators.minLength(14)]],
    chavePix: ['', Validators.maxLength(150)],
    status: ['ATIVO']
  });

  get f() { return this.investidorForm.controls; }

  ngOnInit() {
    this.carregarInvestidores();
  }

  carregarInvestidores() {
    this.loading.set(true);
    this.service.listarInvestidores().subscribe({
      next: (res) => {
        this.investidores.set(res.data || []);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  editar(investidor: any) {
    this.investidorEmEdicaoId = investidor.id;
    this.investidorForm.patchValue({
      nome: investidor.nome,
      telefone: AppFormatter.telefone(investidor.telefone), // Formata ao jogar pra tela
      chavePix: investidor.chavePix,
      status: investidor.status
    });
  }

  cancelarEdicao() {
    this.investidorEmEdicaoId = null;
    this.investidorForm.reset({ status: 'ATIVO' });
  }

  salvar() {
    if (this.investidorForm.invalid) {
      this.investidorForm.markAllAsTouched();
      return;
    }

    const payload = {
      ...this.investidorForm.value,
      telefone: this.investidorForm.value.telefone.replace(/\D/g, '') // Remove máscara antes do backend
    };

    const operacao$ = this.investidorEmEdicaoId
      ? this.service.atualizarInvestidor(this.investidorEmEdicaoId, payload)
      : this.service.salvarInvestidor(payload);

    operacao$.subscribe({
      next: () => {
        this.carregarInvestidores();
        this.cancelarEdicao();
      },
      error: (err) => alert(err.error?.message || 'Erro ao salvar investidor')
    });
  }

  inativar(id: number) {
    if (confirm('Tem certeza que deseja inativar (excluir) este sócio/investidor?')) {
      this.service.inativarInvestidor(id).subscribe({
        next: () => this.carregarInvestidores(),
        error: () => alert('Erro ao inativar.')
      });
    }
  }

  abrirFichaFinanceira(id: number) {
    this.exibirRelatorio.set(true);
    this.loadingRelatorio.set(true);
    this.dadosRelatorio.set(null); // Reseta os dados antigos

    this.service.obterRelatorioInvestidor(id).subscribe({
      next: (res) => {
        this.dadosRelatorio.set(res.data);
        this.loadingRelatorio.set(false);
      },
      error: () => {
        alert('Erro ao carregar o relatório financeiro.');
        this.fecharFichaFinanceira();
      }
    });
  }

  fecharFichaFinanceira() {
    this.exibirRelatorio.set(false);
    this.dadosRelatorio.set(null);
  }
}
