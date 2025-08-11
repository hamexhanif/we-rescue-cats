import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, User } from '../models/auth-model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private tokenKey = 'jwt_token';
  private userKey = 'user_data';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadUserFromStorage();
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          if (response.success && response.token) {
            this.setCurrentUser(response.user, response.token);
          }
        }),
        catchError(error => {
          console.error('Login error:', error);
          return throwError(() => error);
        })
      );
  }

  register(userData: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, userData)
      .pipe(
        tap(response => {
          if (response.success) {
            console.log('Registration successful:', response.user);
          }
        }),
        catchError(error => {
          console.error('Registration error:', error);
          return throwError(() => error);
        })
      );
  }


  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSubject.next(null);
    this.isLoggedInSubject.next(false);
    this.router.navigate(['/']);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getUserFullName(): string {
    const user = this.getCurrentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  }

  getUserAddress(): { streetAddress: string | null; postalCode: string | null } | null {
    const user = this.getCurrentUser();
    return user ? {
      streetAddress: user.streetAddress,
      postalCode: user.postalCode
    } : null;
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;
    
    // Check if token is expired
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const isExpired = payload.exp * 1000 < Date.now();
      
      if (isExpired) {
        this.logout();
        return false;
      }
      
      return true;
    } catch {
      return false;
    }
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'ADMIN';
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private setCurrentUser(user: User, token: string): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userKey, JSON.stringify(user));
    this.currentUserSubject.next(user);
    this.isLoggedInSubject.next(true);
  }

  private loadUserFromStorage(): void {
    const userJson = localStorage.getItem(this.userKey);
    const token = localStorage.getItem(this.tokenKey);
    
    if (userJson && token && this.isAuthenticated()) {
      try {
        const user = JSON.parse(userJson);
        this.currentUserSubject.next(user);
        this.isLoggedInSubject.next(true);
      } catch (error) {
        console.error('Error parsing user data:', error);
        this.logout();
      }
    }
  }
}