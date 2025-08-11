import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminApplicationsComponent } from './admin-applications';
import { AdminService } from '../../../services/admin-service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { AdoptionApplication } from '../../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('AdminApplicationsComponent', () => {
  let component: AdminApplicationsComponent;
  let fixture: ComponentFixture<AdminApplicationsComponent>;
  let adminService: any;
  let snackBar: any;
  let dialog: any;

  const mockApplications: AdoptionApplication[] = [
    {
      id: 1,
      user: {
        id: 1,
        fullName: 'John Doe',
        email: 'john@example.com',
        phone: '+1234567890'
      },
      cat: {
        id: 1,
        name: 'Luna',
        breed: 'Persian Mix'
      },
      adoptionDate: '2024-01-20T10:00:00Z',
      status: 'PENDING'
    },
    {
      id: 2,
      user: {
        id: 2,
        fullName: 'Jane Smith',
        email: 'jane@example.com',
        phone: '+0987654321'
      },
      cat: {
        id: 2,
        name: 'Max',
        breed: 'Siamese'
      },
      adoptionDate: '2024-01-18T10:00:00Z',
      status: 'APPROVED'
    },
    {
      id: 3,
      user: {
        id: 3,
        fullName: 'Bob Wilson',
        email: 'bob@example.com'
      },
      cat: {
        id: 3,
        name: 'Bella',
        breed: 'Maine Coon'
      },
      adoptionDate: '2024-01-16T10:00:00Z',
      status: 'COMPLETED'
    }
  ];

  beforeEach(async () => {
    const adminServiceSpy = {
      getAllApplications: jasmine.createSpy('getAllApplications').and.returnValue(of(mockApplications)),
      approveApplication: jasmine.createSpy('approveApplication').and.returnValue(of({})),
      completeApplication: jasmine.createSpy('completeApplication').and.returnValue(of({})),
      rejectApplication: jasmine.createSpy('rejectApplication').and.returnValue(of({}))
    };
    
    const snackBarSpy = {
      open: jasmine.createSpy('open')
    };
    
    const dialogSpy = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [
        AdminApplicationsComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: MatDialog, useValue: dialogSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminApplicationsComponent);
    component = fixture.componentInstance;
    adminService = TestBed.inject(AdminService);
    snackBar = TestBed.inject(MatSnackBar);
    dialog = TestBed.inject(MatDialog);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty applications array', () => {
    expect(component.applications).toEqual([]);
  });

  it('should have correct displayedColumns', () => {
    expect(component.displayedColumns).toEqual(['applicant', 'cat', 'submittedAt', 'status', 'actions']);
  });

  it('should start with counts = 0', () => {
    expect(component.pendingCount).toBe(0);
    expect(component.approvedCount).toBe(0);
  });

  it('should call loadApplications on ngOnInit', () => {
    spyOn(component, 'loadApplications');
    
    component.ngOnInit();
    
    expect(component.loadApplications).toHaveBeenCalled();
  });

  it('should load applications successfully', () => {
    component.loadApplications();
    
    expect(adminService.getAllApplications).toHaveBeenCalled();
    expect(component.applications).toEqual(mockApplications);
    expect(component.pendingCount).toBe(1); // One PENDING application
    expect(component.approvedCount).toBe(1); // One APPROVED application
  });

  it('should handle error when loading applications', () => {
    spyOn(console, 'error');
    adminService.getAllApplications.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadApplications();
    
    expect(console.error).toHaveBeenCalledWith('Error loading applications:', jasmine.any(Error));
    expect(snackBar.open).toHaveBeenCalledWith('Error loading applications', 'Close', { duration: 3000 });
  });

  describe('approveApplication', () => {
    const pendingApplication = mockApplications[0];

    it('should approve application successfully', () => {
      spyOn(component, 'loadApplications');
      
      component.approveApplication(pendingApplication);
      
      expect(adminService.approveApplication).toHaveBeenCalledWith(1);
      expect(snackBar.open).toHaveBeenCalledWith('Application approved successfully', 'Close', { duration: 3000 });
      expect(component.loadApplications).toHaveBeenCalled();
    });

    it('should handle error when approving application', () => {
      spyOn(console, 'error');
      adminService.approveApplication.and.returnValue(throwError(() => new Error('Approval failed')));
      
      component.approveApplication(pendingApplication);
      
      expect(console.error).toHaveBeenCalledWith('Error approving application:', jasmine.any(Error));
      expect(snackBar.open).toHaveBeenCalledWith('Error approving application', 'Close', { duration: 3000 });
    });
  });

  describe('completeApplication', () => {
    const approvedApplication = mockApplications[1];

    it('should complete application successfully', () => {
      spyOn(component, 'loadApplications');
      
      component.completeApplication(approvedApplication);
      
      expect(adminService.completeApplication).toHaveBeenCalledWith(2);
      expect(snackBar.open).toHaveBeenCalledWith('Adoption completed successfully', 'Close', { duration: 3000 });
      expect(component.loadApplications).toHaveBeenCalled();
    });

    it('should handle error when completing application', () => {
      spyOn(console, 'error');
      adminService.completeApplication.and.returnValue(throwError(() => new Error('Completion failed')));
      
      component.completeApplication(approvedApplication);
      
      expect(console.error).toHaveBeenCalledWith('Error completing application:', jasmine.any(Error));
      expect(snackBar.open).toHaveBeenCalledWith('Error completing application', 'Close', { duration: 3000 });
    });
  });

  describe('rejectApplication', () => {
    const pendingApplication = mockApplications[0];

    it('should reject application with reason', () => {
      spyOn(window, 'prompt').and.returnValue('Not suitable home');
      spyOn(component, 'loadApplications');
      
      component.rejectApplication(pendingApplication);
      
      expect(window.prompt).toHaveBeenCalledWith('Please enter rejection reason:');
      expect(adminService.rejectApplication).toHaveBeenCalledWith(1, 'Not suitable home');
      expect(snackBar.open).toHaveBeenCalledWith('Application rejected', 'Close', { duration: 3000 });
      expect(component.loadApplications).toHaveBeenCalled();
    });

    it('should not reject when no reason provided', () => {
      spyOn(window, 'prompt').and.returnValue(null);
      
      component.rejectApplication(pendingApplication);
      
      expect(window.prompt).toHaveBeenCalled();
      expect(adminService.rejectApplication).not.toHaveBeenCalled();
    });

    it('should not reject when empty reason provided', () => {
      spyOn(window, 'prompt').and.returnValue('');
      
      component.rejectApplication(pendingApplication);
      
      expect(adminService.rejectApplication).not.toHaveBeenCalled();
    });

    it('should handle error when rejecting application', () => {
      spyOn(window, 'prompt').and.returnValue('Not suitable');
      spyOn(console, 'error');
      adminService.rejectApplication.and.returnValue(throwError(() => new Error('Rejection failed')));
      
      component.rejectApplication(pendingApplication);
      
      expect(console.error).toHaveBeenCalledWith('Error rejecting application:', jasmine.any(Error));
      expect(snackBar.open).toHaveBeenCalledWith('Error rejecting application', 'Close', { duration: 3000 });
    });
  });

  describe('status counting', () => {
    it('should count pending and approved applications correctly', () => {
      component.loadApplications();
      
      // mockApplications: 1 PENDING, 1 APPROVED, 1 COMPLETED
      expect(component.pendingCount).toBe(1);
      expect(component.approvedCount).toBe(1);
    });

    it('should handle applications with no status', () => {
      const applicationsWithoutStatus = [
        { ...mockApplications[0], status: undefined },
        { ...mockApplications[1], status: null }
      ] as any;
      
      adminService.getAllApplications.and.returnValue(of(applicationsWithoutStatus));
      
      component.loadApplications();
      
      expect(component.pendingCount).toBe(0);
      expect(component.approvedCount).toBe(0);
    });
  });
});