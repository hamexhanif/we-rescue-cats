export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  streetAddress: string | null;
  postalCode: string | null;
  role: 'USER' | 'ADMIN';
  enabled: boolean,
  createdAt: string;
  lastLogin: string | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token: string;
  user: User;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  streetAddress?: string;
  postalCode?: string;
}

export interface RegisterResponse {
  success: boolean;
  message: string;
  user: User;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
