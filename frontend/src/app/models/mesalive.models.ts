export type MesaStatus = 'LIVRE' | 'RESERVADA' | 'OCUPADA' | 'NO_SHOW' | 'INATIVA';
export type UserRole = 'GARCOM' | 'GERENTE';

export interface Cliente {
  id?: number;
  nome: string;
  telefone: string;
  email?: string;
}

export interface Mesa {
  id: number;
  numero: string;
  capacidade: number;
  ativo: boolean;
  statusAtual: MesaStatus;
  reservaAtivaId?: number;
  nomeCliente?: string;
  dataHoraReserva?: string;
}

export interface Reserva {
  id: number;
  mesaId: number;
  mesa?: Mesa;
  cliente: Cliente;
  dataHora: string;
  quantidadePessoas: number;
  status: 'CONFIRMADA' | 'CLIENTE_CHEGOU' | 'CANCELADA' | 'NO_SHOW';
}

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
  nome: string;
  email: string;
  role: UserRole;
}
