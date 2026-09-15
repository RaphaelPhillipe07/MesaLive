import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mesa, Reserva, Usuario, LoginResponse } from '../models/mesalive.models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Auth
  login(credentials: { email: string; senha: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, credentials);
  }

  cadastrarUsuario(usuario: Partial<Usuario> & { senha: string }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.baseUrl}/usuarios`, usuario);
  }

  // Mesas
  getMesas(): Observable<Mesa[]> {
    return this.http.get<Mesa[]>(`${this.baseUrl}/mesas`);
  }

  getMesasPublicas(): Observable<Mesa[]> {
    return this.http.get<Mesa[]>(`${this.baseUrl}/mesas/publicas`);
  }

  atualizarMesaStatus(id: number, ativo: boolean): Observable<Mesa> {
    return this.http.patch<Mesa>(`${this.baseUrl}/mesas/${id}/status`, { ativo });
  }

  // Reservas
  criarReserva(reserva: {
    mesaId: number;
    nomeCliente: string;
    telefoneCliente: string;
    emailCliente?: string | null;
    dataHora: string;
    quantidadePessoas: number;
  }): Observable<Reserva> {
    return this.http.post<Reserva>(`${this.baseUrl}/reservas`, reserva);
  }

  consultarReserva(id: number, telefone: string): Observable<Reserva> {
    const params = new HttpParams().set('telefone', telefone);
    return this.http.get<Reserva>(`${this.baseUrl}/reservas/${id}`, { params });
  }

  cancelarReserva(id: number, telefone: string): Observable<Reserva> {
    const params = new HttpParams().set('telefone', telefone);
    return this.http.delete<Reserva>(`${this.baseUrl}/reservas/${id}`, { params });
  }

  atualizarReservaStatus(id: number, status: string): Observable<Reserva> {
    return this.http.patch<Reserva>(`${this.baseUrl}/reservas/${id}/status`, { status });
  }
}
