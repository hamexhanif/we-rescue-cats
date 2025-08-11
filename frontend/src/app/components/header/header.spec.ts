import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header';
import { AuthService } from '../../services/auth-service';
import { User } from '../../models/auth-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authService: any;

  const mockUser: User = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    role: 'USER',
    streetAddress: '123 Main St',
    postalCode: '12345',
    createdAt: '2024-01-01T00:00:00Z',
    enabled: true,
    lastLogin: '2024-01-01T00:00:00Z'
  };

  const mockAdminUser: User = {
    id: 2,
    firstName: 'Admin',
    lastName: 'User',
    email: 'admin@example.com',
    role: 'ADMIN',
    streetAddress: '456 Admin St',
    postalCode: '67890',
    createdAt: '2024-01-01T00:00:00Z',
    enabled: true,
    lastLogin: '2024-01-01T00:00:00Z'
  };

  beforeEach(async () => {
    const authServiceSpy = {
      currentUser$: of(null),
      isAdmin: jasmine.createSpy('isAdmin').and.returnValue(false),
      logout: jasmine.createSpy('logout')
    };

    await TestBed.configureTestingModule({
      imports: [
        HeaderComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize currentUser$ from AuthService', () => {
    expect(component.currentUser$).toBeDefined();
    expect(component.currentUser$).toBe(authService.currentUser$);
  });

  it('should call ngOnInit without errors', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  describe('isAdmin', () => {
    it('should return false for regular user', () => {
      authService.isAdmin.and.returnValue(false);
      
      expect(component.isAdmin()).toBe(false);
      expect(authService.isAdmin).toHaveBeenCalled();
    });

    it('should return true for admin user', () => {
      authService.isAdmin.and.returnValue(true);
      
      expect(component.isAdmin()).toBe(true);
      expect(authService.isAdmin).toHaveBeenCalled();
    });
  });

  describe('logout', () => {
    it('should call AuthService logout', () => {
      component.logout();
      
      expect(authService.logout).toHaveBeenCalled();
    });
  });

  describe('currentUser$ observable', () => {
    it('should handle null user (not logged in)', () => {
      authService.currentUser$ = of(null);
      component.currentUser$ = authService.currentUser$;
      
      component.currentUser$.subscribe(user => {
        expect(user).toBe(null);
      });
    });

    it('should handle regular user', () => {
      authService.currentUser$ = of(mockUser);
      component.currentUser$ = authService.currentUser$;
      
      component.currentUser$.subscribe(user => {
        expect(user).toEqual(mockUser);
        expect(user?.firstName).toBe('John');
        expect(user?.role).toBe('USER');
      });
    });

    it('should handle admin user', () => {
      authService.currentUser$ = of(mockAdminUser);
      component.currentUser$ = authService.currentUser$;
      
      component.currentUser$.subscribe(user => {
        expect(user).toEqual(mockAdminUser);
        expect(user?.firstName).toBe('Admin');
        expect(user?.role).toBe('ADMIN');
      });
    });
  });

  describe('component integration', () => {
    it('should handle user login state change', () => {
      authService.currentUser$ = of(null);
      component.currentUser$ = authService.currentUser$;
      
      component.currentUser$.subscribe(user => {
        expect(user).toBe(null);
      });

      authService.currentUser$ = of(mockUser);
      component.currentUser$ = authService.currentUser$;
      
      component.currentUser$.subscribe(user => {
        expect(user).toEqual(mockUser);
      });
    });

    it('should properly handle admin check with different users', () => {
      authService.isAdmin.and.returnValue(false);
      expect(component.isAdmin()).toBe(false);

      authService.isAdmin.and.returnValue(true);
      expect(component.isAdmin()).toBe(true);
    });
  });
});