import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register';
import { AuthService } from '../../../services/auth-service';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authService: any;
  let router: any;
  let snackBar: any;

  beforeEach(async () => {
    const mockAuthService = {
      register: jasmine.createSpy('register').and.returnValue(of({ 
        success: true, 
        message: 'Registration successful', 
        user: { 
          id: 1, 
          email: 'john@example.com', 
          firstName: 'John', 
          lastName: 'Doe' 
        } 
      }))
    };
    const mockRouter = {
      navigate: jasmine.createSpy('navigate')
    };
    const mockSnackBar = {
      open: jasmine.createSpy('open')
    };
    const mockFormBuilder = new FormBuilder();
    const mockActivatedRoute = {
      snapshot: { queryParams: {} },
      queryParams: of({})
    };

    await TestBed.configureTestingModule({
      imports: [
        RegisterComponent,
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

    fixture = TestBed.createComponent(RegisterComponent);
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

  it('should have registerForm defined', () => {
    expect(component.registerForm).toBeDefined();
  });

  it('should have all required form controls', () => {
    expect(component.registerForm.get('firstName')).toBeTruthy();
    expect(component.registerForm.get('lastName')).toBeTruthy();
    expect(component.registerForm.get('email')).toBeTruthy();
    expect(component.registerForm.get('password')).toBeTruthy();
    expect(component.registerForm.get('confirmPassword')).toBeTruthy();
    expect(component.registerForm.get('address')).toBeTruthy();
    expect(component.registerForm.get('postalCode')).toBeTruthy();
  });

  it('should start with invalid form', () => {
    expect(component.registerForm.valid).toBe(false);
  });

  it('should be valid when all fields are filled correctly', () => {
    component.registerForm.patchValue({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      password: 'Password123',
      confirmPassword: 'Password123',
      address: '123 Main St',
      postalCode: '12345'
    });

    expect(component.registerForm.valid).toBe(true);
  });

  it('should show password mismatch error', () => {
    component.registerForm.patchValue({
      password: 'Password123',
      confirmPassword: 'DifferentPassword'
    });

    expect(component.registerForm.hasError('passwordMismatch')).toBe(true);
  });

  it('should call AuthService register when form is valid and submitted', () => {
    component.registerForm.patchValue({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      password: 'Password123',
      confirmPassword: 'Password123',
      address: '123 Main St',
      postalCode: '12345'
    });

    component.onSubmit();

    expect(authService.register).toHaveBeenCalledWith({
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      password: 'Password123',
      streetAddress: '123 Main St',
      postalCode: '12345'
    });
  });

  it('should not submit when form is invalid', () => {
    component.registerForm.patchValue({
      email: 'invalid-email'
    });

    component.onSubmit();

    expect(authService.register).not.toHaveBeenCalled();
  });

  it('should test passwordMatchValidator directly', () => {
    const formGroup = component.registerForm;
    
    // Test matching passwords
    formGroup.patchValue({
      password: 'Password123',
      confirmPassword: 'Password123'
    });
    expect(component.passwordMatchValidator(formGroup)).toBe(null);

    // Test non-matching passwords
    formGroup.patchValue({
      password: 'Password123',
      confirmPassword: 'Different123'
    });
    expect(component.passwordMatchValidator(formGroup)).toEqual({ passwordMismatch: true });
  });
});