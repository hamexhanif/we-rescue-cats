import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login';
import { AuthService } from '../../../services/auth-service';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: any;
  let router: any;
  let snackBar: any;

  beforeEach(async () => {
    const mockAuthService = {
      login: jasmine.createSpy('login').and.returnValue(of({ 
        success: true, 
        message: 'Login successful', 
        user: { 
          id: 1, 
          email: 'john@example.com', 
          firstName: 'John', 
          lastName: 'Doe' 
        },
        token: 'fake-jwt-token'
      }))
    };
    const mockRouter = {
      navigateByUrl: jasmine.createSpy('navigateByUrl')
    };
    const mockSnackBar = {
      open: jasmine.createSpy('open')
    };
    const mockFormBuilder = new FormBuilder();
    const mockActivatedRoute = {
      snapshot: { queryParams: { returnUrl: '/profile' } },
      queryParams: of({ returnUrl: '/profile' })
    };

    await TestBed.configureTestingModule({
      imports: [
        LoginComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: FormBuilder, useValue: mockFormBuilder },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
    snackBar = TestBed.inject(MatSnackBar);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with loading = false', () => {
    expect(component.loading).toBe(false);
  });

  it('should have loginForm defined', () => {
    expect(component.loginForm).toBeDefined();
  });

  it('should have correct form controls', () => {
    expect(component.loginForm.get('email')).toBeTruthy();
    expect(component.loginForm.get('password')).toBeTruthy();
  });

  it('should start with invalid form', () => {
    expect(component.loginForm.valid).toBe(false);
  });

  it('should be valid when both email and password are provided', () => {
    component.loginForm.patchValue({
      email: 'john@example.com',
      password: 'password123'
    });

    expect(component.loginForm.valid).toBe(true);
  });

  it('should set returnUrl from route params', () => {
    expect(component.returnUrl).toBe('/profile');
  });

  it('should call AuthService login when form is valid and submitted', () => {
    component.loginForm.patchValue({
      email: 'john@example.com',
      password: 'password123'
    });

    component.onSubmit();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'john@example.com',
      password: 'password123'
    });
  });

  it('should not submit when form is invalid', () => {
    component.loginForm.patchValue({
      email: '',
      password: 'password123'
    });

    component.onSubmit();

    expect(authService.login).not.toHaveBeenCalled();
  });

  it('should require email field', () => {
    const emailControl = component.loginForm.get('email');
    expect(emailControl?.hasError('required')).toBe(true);

    emailControl?.setValue('test@example.com');
    expect(emailControl?.hasError('required')).toBe(false);
  });

  it('should require password field', () => {
    const passwordControl = component.loginForm.get('password');
    expect(passwordControl?.hasError('required')).toBe(true);

    passwordControl?.setValue('password123');
    expect(passwordControl?.hasError('required')).toBe(false);
  });

  it('should have correct form structure', () => {
    const form = component.loginForm;
    expect(form.get('email')?.validator).toBeTruthy();
    expect(form.get('password')?.validator).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.loginForm.get('email')?.value).toBe('');
    expect(component.loginForm.get('password')?.value).toBe('');
  });
});