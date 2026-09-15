import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, timer } from 'rxjs';
import { switchMap, filter } from 'rxjs/operators';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { Mesa, Usuario } from '../../models/mesalive.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  // Signals para estado Reativo e Otimizado
  mesas = signal<Mesa[]>([]);
  isLoading = signal<boolean>(false);
  usuario = signal<Usuario | null>(null);

  // Computeds reativos
  countTotal = computed(() => this.mesas().length);
  countLivre = computed(() => this.mesas().filter(m => m.statusAtual === 'LIVRE').length);
  countReservada = computed(() => this.mesas().filter(m => m.statusAtual === 'RESERVADA').length);
  countOcupada = computed(() => this.mesas().filter(m => m.statusAtual === 'OCUPADA').length);
  countNoShow = computed(() => this.mesas().filter(m => m.statusAtual === 'NO_SHOW').length);

  // Cadastro de Staff (Apenas Gerente)
  newUser = { nome: '', email: '', senha: '', role: 'GARCOM' as 'GARCOM' | 'GERENTE' };
  registrationSuccess = '';
  registrationError = '';
  isRegistering = false;

  private pollSubscription: Subscription | null = null;

  constructor(
    private apiService: ApiService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
      return;
    }

    this.usuario.set(this.authService.currentUser());
    this.iniciarSmartPolling();
  }

  ngOnDestroy() {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
    }
  }

  /**
   * Smart Polling com RxJS:
   * Consulta a API a cada 8 segundos, apenas quando a aba estiver ativa/visível para economizar recursos.
   */
  private iniciarSmartPolling() {
    this.isLoading.set(true);
    this.pollSubscription = timer(0, 8000)
      .pipe(
        filter(() => document.visibilityState === 'visible'),
        switchMap(() => this.apiService.getMesas())
      )
      .subscribe({
        next: (dados) => {
          this.mesas.set(dados);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.isLoading.set(false);
          console.error('Erro ao carregar mesas via Smart Polling:', err);
          if (err.status === 401 || err.status === 403) {
            this.authService.logout();
            this.router.navigate(['/login']);
          }
        }
      });
  }

  carregarDados() {
    this.apiService.getMesas().subscribe({
      next: (res) => this.mesas.set(res),
      error: (err) => console.error('Erro ao recarregar dados:', err)
    });
  }

  // --- Ações com Atualização UI Otimista ---

  toggleAtivo(mesa: Mesa) {
    const novoAtivo = !mesa.ativo;
    
    // Atualização otimista local imediata
    this.mesas.update(lista =>
      lista.map(m => m.id === mesa.id ? { ...m, ativo: novoAtivo, statusAtual: novoAtivo ? 'LIVRE' : 'INATIVA' } : m)
    );

    this.apiService.atualizarMesaStatus(mesa.id, novoAtivo).subscribe({
      error: (err) => {
        console.error('Falha ao atualizar mesa, revertendo...', err);
        this.carregarDados();
      }
    });
  }

  marcarEntrada(reservaId: number, mesaId: number) {
    // Atualização otimista
    this.mesas.update(lista =>
      lista.map(m => m.id === mesaId ? { ...m, statusAtual: 'OCUPADA' } : m)
    );

    this.apiService.atualizarReservaStatus(reservaId, 'CLIENTE_CHEGOU').subscribe({
      next: () => this.carregarDados(),
      error: (err) => {
        console.error(err);
        this.carregarDados();
      }
    });
  }

  marcarNoShow(reservaId: number, mesaId: number) {
    // Atualização otimista
    this.mesas.update(lista =>
      lista.map(m => m.id === mesaId ? { ...m, statusAtual: 'NO_SHOW' } : m)
    );

    this.apiService.atualizarReservaStatus(reservaId, 'NO_SHOW').subscribe({
      next: () => this.carregarDados(),
      error: (err) => {
        console.error(err);
        this.carregarDados();
      }
    });
  }

  liberarMesa(reservaId: number, mesaId: number) {
    // Atualização otimista
    this.mesas.update(lista =>
      lista.map(m => m.id === mesaId ? { ...m, statusAtual: 'LIVRE', reservaAtivaId: undefined, nomeCliente: undefined } : m)
    );

    this.apiService.atualizarReservaStatus(reservaId, 'CANCELADA').subscribe({
      next: () => this.carregarDados(),
      error: (err) => {
        console.error(err);
        this.carregarDados();
      }
    });
  }

  cadastrarStaff() {
    if (!this.newUser.nome || !this.newUser.email || !this.newUser.senha || !this.newUser.role) {
      this.registrationError = 'Preencha todos os campos do formulário.';
      return;
    }
    this.isRegistering = true;
    this.registrationSuccess = '';
    this.registrationError = '';

    this.apiService.cadastrarUsuario(this.newUser).subscribe({
      next: (res) => {
        this.isRegistering = false;
        this.registrationSuccess = `Funcionário ${res.nome} cadastrado com sucesso!`;
        this.newUser = { nome: '', email: '', senha: '', role: 'GARCOM' };
      },
      error: (err) => {
        this.isRegistering = false;
        this.registrationError = err.error?.message || 'Erro ao realizar o cadastro.';
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  formatHour(isoStr?: string): string {
    if (!isoStr) return '';
    const date = new Date(isoStr);
    return date.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }
}
