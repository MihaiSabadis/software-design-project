import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL_LOG = 'http://localhost:8080/login';
const API_URL_REG = 'http://localhost:8080/person';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  role: string | null;
  errorMessage: string | null;
  token: string | null;
  userId: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(API_URL_LOG, request);
  }

  register(personData: any): Observable<any>{
    return this.http.post(API_URL_REG, personData, {responseType: 'text'});
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(API_URL_REG + `/forgot-password?email=${email}`, {}, { responseType: 'text' });
  }

  resetPassword(email: string, code: string, newPassword: string): Observable<any> {
    return this.http.post(
      API_URL_REG + `/reset-password?email=${email}&code=${code}&newPassword=${newPassword}`,
      {},
      { responseType: 'text' },
    );
  }
}
