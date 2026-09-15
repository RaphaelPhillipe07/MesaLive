import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { Mesa, Reserva } from '../../models/mesalive.models';

@Component({
  selector: 'app-reserva-cliente',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reserva-cliente.component.html',
  styleUrls: ['./reserva-cliente.component.css']
})
export class ReservaClienteComponent implements OnInit {
  activeTab = signal<'nova' | 'minha'>('nova');

  // Form de criação
  booking = {
    nomeCliente: '',
    telefoneCliente: '',
    emailCliente: '',
    mesaId: null as number | null,
    dataHora: '',
    quantidadePessoas: 2
  };

  // Form de consulta
  lookup = {
    id: null as number | null,
    telefone: ''
  };

  isLoading = signal<boolean>(false);
  successMessage = signal<string>('');
  errorMessage = signal<string>('');

  createdBooking = signal<Reserva | null>(null);
  searchedBooking = signal<Reserva | null>(null);

  mesasDisponiveis = signal<Mesa[]>([]);

  constructor(
    private apiService: ApiService,
    public router: Router
  ) {}

  ngOnInit() {
    this.carregarMesas();
  }

  carregarMesas() {
    this.apiService.getMesasPublicas().subscribe({
      next: (res) => this.mesasDisponiveis.set(res),
      error: () => {
        // Fallback em desenvolvimento
        this.mesasDisponiveis.set([
          { id: 1, numero: 'Mesa 01', capacidade: 2, ativo: true, statusAtual: 'LIVRE' },
          { id: 2, numero: 'Mesa 02', capacidade: 2, ativo: true, statusAtual: 'LIVRE' },
          { id: 3, numero: 'Mesa 03', capacidade: 4, ativo: true, statusAtual: 'LIVRE' },
          { id: 4, numero: 'Mesa 04', capacidade: 4, ativo: true, statusAtual: 'LIVRE' },
          { id: 5, numero: 'Mesa 05', capacidade: 6, ativo: true, statusAtual: 'LIVRE' },
          { id: 6, numero: 'Mesa 06', capacidade: 8, ativo: true, statusAtual: 'LIVRE' }
        ]);
      }
    });
  }

  setTab(tab: 'nova' | 'minha') {
    this.activeTab.set(tab);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.createdBooking.set(null);
    this.searchedBooking.set(null);
  }

  selecionarMesa(mesaId: number) {
    this.booking.mesaId = mesaId;
  }

  onSubmitReserva() {
    if (!this.booking.nomeCliente || !this.booking.telefoneCliente || !this.booking.dataHora || !this.booking.mesaId) {
      this.errorMessage.set('Por favor, preencha todos os campos obrigatórios e escolha uma mesa.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const offsetDate = new Date(this.booking.dataHora).toISOString();

    const payload = {
      mesaId: this.booking.mesaId,
      nomeCliente: this.booking.nomeCliente,
      telefoneCliente: this.booking.telefoneCliente,
      emailCliente: this.booking.emailCliente || null,
      dataHora: offsetDate,
      quantidadePessoas: this.booking.quantidadePessoas
    };

    this.apiService.criarReserva(payload).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.createdBooking.set(res);
        this.successMessage.set('Sua reserva foi confirmada com sucesso!');
        this.resetForm();
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409) {
          this.errorMessage.set('Esta mesa já possui reserva neste horário. Por favor, escolha outro horário ou mesa.');
        } else if (err.status === 400) {
          this.errorMessage.set(err.error?.message || 'Dados inválidos. Verifique as informações fornecidas.');
        } else {
          this.errorMessage.set('Erro ao conectar com o servidor. Tente novamente mais tarde.');
        }
      }
    });
  }

  onConsultar() {
    if (!this.lookup.id || !this.lookup.telefone) {
      this.errorMessage.set('Informe o código da reserva (#) e o telefone de contato.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.searchedBooking.set(null);

    this.apiService.consultarReserva(this.lookup.id, this.lookup.telefone).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.searchedBooking.set(res);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.errorMessage.set('Reserva não encontrada. Verifique o código e o telefone.');
        } else {
          this.errorMessage.set('Telefone incorreto ou falha de conexão.');
        }
      }
    });
  }

  onCancelar(id: number, telefone: string) {
    if (!confirm('Deseja realmente cancelar esta reserva?')) return;

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.apiService.cancelarReserva(id, telefone).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        alert('Reserva cancelada com sucesso!');
        this.searchedBooking.set(res);
      },
      error: () => {
        this.isLoading.set(false);
        this.errorMessage.set('Não foi possível cancelar a reserva. Tente novamente.');
      }
    });
  }

  formatDate(dateStr?: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' });
  }

  resetForm() {
    this.booking = {
      nomeCliente: '',
      telefoneCliente: '',
      emailCliente: '',
      mesaId: null,
      dataHora: '',
      quantidadePessoas: 2
    };
  }
}
