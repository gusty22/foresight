import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AuditoriaComponent } from './auditoria';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

describe('AuditoriaComponent', () => {
  let component: AuditoriaComponent;
  let fixture: ComponentFixture<AuditoriaComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditoriaComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    fixture = TestBed.createComponent(AuditoriaComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('deve carregar logs e exibir na tabela', () => {
    const mockLogs = [
      { id: 1, usuarioEmail: 'gustavo@teste.com', acao: 'CRIACAO', entidadeNome: 'Produto', dataHora: new Date() }
    ];

    fixture.detectChanges();

    const req = httpMock.expectOne('http://localhost:8080/api/auditoria');
    expect(req.request.method).toBe('GET');
    req.flush(mockLogs);

    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('td.fw-bold').textContent).toContain('gustavo@teste.com');
    expect(compiled.querySelector('.badge').textContent).toContain('CRIACAO');
  });
});
