import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HistoricoVendas } from './historico-vendas';

describe('HistoricoVendas', () => {
  let component: HistoricoVendas;
  let fixture: ComponentFixture<HistoricoVendas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HistoricoVendas]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HistoricoVendas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
