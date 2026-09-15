import { Injectable, signal } from '@angular/core';
import { Usuario } from '../models/mesalive.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private tokenKey = 'mesalive_token';
  private userKey = 'mesalive_user';

  currentUser = signal<Usuario | null>(null);

  constructor() {
    this.loadUser();
  }

  login(token: string, user: Usuario) {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.currentUser.set(user);
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private loadUser() {
    const userStr = localStorage.getItem(this.userKey);
    if (userStr) {
      try {
        this.currentUser.set(JSON.parse(userStr));
      } catch (e) {
        this.logout();
      }
    }
  }
}
