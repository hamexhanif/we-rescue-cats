import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User, ApiResponse } from '../models/auth-model';
import { AdoptionApplication, Cat, CreateCatRequest } from '../models/cat-model';
import { DashboardStats } from '../models/dashboard-stats-model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  deleteUser(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/users/${id}`);
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`);
  }
  
  getAllApplications(): Observable<AdoptionApplication[]> {
    return this.http.get<AdoptionApplication[]>(`${this.apiUrl}/adoptions`);
  }
  
  approveApplication(id: number): Observable<AdoptionApplication> {
    return this.http.put<AdoptionApplication>(`${this.apiUrl}/adoptions/${id}/approve`, {});
  }
  
  // Complete adoption when cat picked up
  completeApplication(id: number): Observable<AdoptionApplication> {
    return this.http.put<AdoptionApplication>(`${this.apiUrl}/adoptions/${id}/complete`, {});
  }
  
  rejectApplication(id: number, reason: string): Observable<AdoptionApplication> {
    return this.http.put<AdoptionApplication>(`${this.apiUrl}/adoptions/${id}/reject`, {
      reason: reason
    });
  }

  // Admin-only endpoints
    createCat(catData: CreateCatRequest): Observable<ApiResponse<Cat>> {
      return this.http.post<ApiResponse<Cat>>(`${this.apiUrl}/cats/admin/create`, catData);
    }
  
    updateCat(id: number, catData: Partial<CreateCatRequest>): Observable<ApiResponse<Cat>> {
      return this.http.put<ApiResponse<Cat>>(`${this.apiUrl}/admin/cats/${id}`, catData);
    }
  
    deleteCat(id: number): Observable<ApiResponse<void>> {
      return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/admin/cats/${id}`);
    }
}
