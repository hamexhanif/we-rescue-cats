import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminUsersComponent } from './admin-users';
import { AdminService } from '../../../services/admin-service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from '../../../models/auth-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('AdminUsersComponent', () => {
  let component: AdminUsersComponent;
  let fixture: ComponentFixture<AdminUsersComponent>;
  let adminService: any;
  let snackBar: any;

  const mockUsers: User[] = [
    {
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
    },
    {
      id: 2,
      firstName: 'Jane',
      lastName: 'Smith',
      email: 'jane@example.com',
      role: 'ADMIN',
      streetAddress: '456 Oak Ave',
      postalCode: '67890',
      createdAt: '2024-01-02T00:00:00Z',
      enabled: true,
      lastLogin: '2024-01-01T00:00:00Z'
    },
    {
      id: 3,
      firstName: 'Bob',
      lastName: 'Wilson',
      email: 'bob@example.com',
      role: 'USER',
      streetAddress: '789 Pine St',
      postalCode: '11111',
      createdAt: '2024-01-03T00:00:00Z',
      enabled: true,
      lastLogin: '2024-01-01T00:00:00Z'
    }
  ];

  beforeEach(async () => {
    const adminServiceSpy = {
      getAllUsers: jasmine.createSpy('getAllUsers').and.returnValue(of(mockUsers)),
      deleteUser: jasmine.createSpy('deleteUser').and.returnValue(of({ success: true }))
    };
    
    const snackBarSpy = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [
        AdminUsersComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminUsersComponent);
    component = fixture.componentInstance;
    adminService = TestBed.inject(AdminService);
    snackBar = TestBed.inject(MatSnackBar);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty users array', () => {
    expect(component.users).toEqual([]);
  });

  it('should have correct displayedColumns', () => {
    expect(component.displayedColumns).toEqual(['user', 'role', 'createdAt', 'actions']);
  });

  it('should start with adminCount = 0', () => {
    expect(component.adminCount).toBe(0);
  });

  it('should call loadUsers on ngOnInit', () => {
    spyOn(component, 'loadUsers');
    
    component.ngOnInit();
    
    expect(component.loadUsers).toHaveBeenCalled();
  });

  it('should load users successfully', () => {
    component.loadUsers();
    
    expect(adminService.getAllUsers).toHaveBeenCalled();
    expect(component.users).toEqual(mockUsers);
    expect(component.adminCount).toBe(1); // One admin in mock data
  });

  it('should handle null users response', () => {
    adminService.getAllUsers.and.returnValue(of(null));
    
    component.loadUsers();
    
    expect(component.users).toEqual([]);
    expect(component.adminCount).toBe(0);
  });

  it('should handle error when loading users', () => {
    spyOn(console, 'error');
    adminService.getAllUsers.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadUsers();
    
    expect(console.error).toHaveBeenCalledWith('Error loading users:', jasmine.any(Error));
    expect(snackBar.open).toHaveBeenCalledWith('Error loading users', 'Close', { duration: 3000 });
  });

  it('should count admin users correctly', () => {
    component.loadUsers();
    
    // mockUsers has 1 admin (Jane Smith)
    expect(component.adminCount).toBe(1);
  });

  describe('deleteUser', () => {
    const regularUser = mockUsers[0]; // John Doe (USER role)
    const adminUser = mockUsers[1]; // Jane Smith (ADMIN role)

    it('should prevent deletion of admin users', () => {
      component.deleteUser(adminUser);
      
      expect(snackBar.open).toHaveBeenCalledWith('Cannot delete admin users', 'Close', { duration: 3000 });
      expect(adminService.deleteUser).not.toHaveBeenCalled();
    });

    it('should delete regular user when confirmed', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(component, 'loadUsers');
      
      component.deleteUser(regularUser);
      
      expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete user john@example.com?');
      expect(adminService.deleteUser).toHaveBeenCalledWith(1);
      expect(snackBar.open).toHaveBeenCalledWith('User deleted successfully', 'Close', { duration: 3000 });
      expect(component.loadUsers).toHaveBeenCalled();
    });

    it('should not delete when user cancels confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      
      component.deleteUser(regularUser);
      
      expect(window.confirm).toHaveBeenCalled();
      expect(adminService.deleteUser).not.toHaveBeenCalled();
    });

    it('should handle delete error', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(console, 'error');
      adminService.deleteUser.and.returnValue(throwError(() => new Error('Delete failed')));
      
      component.deleteUser(regularUser);
      
      expect(console.error).toHaveBeenCalledWith('Error deleting user:', jasmine.any(Error));
      expect(snackBar.open).toHaveBeenCalledWith('Error deleting user', 'Close', { duration: 3000 });
    });
  });
});