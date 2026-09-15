import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  credentials = {
    email: '',
    senha: ''
  };

  errorMessage = signal<string>('');
  isLoading = signal<boolean>(false);

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    public router: Router
  ) {}

  onSubmit() {
    if (!this.credentials.email || !this.credentials.senha) {
      this.errorMessage.set('Preencha todos os campos para continuar.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.apiService.login(this.credentials).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.authService.login(res.token, { nome: res.nome, email: res.email, role: res.role });
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 400 || err.status === 401 || err.status === 403) {
          this.errorMessage.set('E-mail ou senha incorretos.');
        } else {
          this.errorMessage.set('Falha ao conectar ao servidor. Verifique sua conexão.');
        }
      }
    });
  }
}
