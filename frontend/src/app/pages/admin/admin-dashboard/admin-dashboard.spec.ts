import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminDashboardComponent } from './admin-dashboard';
import { AdminService } from '../../../services/admin-service';
import { DashboardStats } from '../../../models/dashboard-stats-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('AdminDashboardComponent', () => {
  let component: AdminDashboardComponent;
  let fixture: ComponentFixture<AdminDashboardComponent>;
  let adminService: any;

  const mockStats: DashboardStats = {
    totalCats: 150,
    availableCats: 120,
    adoptedCats: 85,
    totalUsers: 1200,
    adminUsers: 5,
    totalAdoptions: 200,
    pendingAdoptions: 15,
    completedAdoptions: 185
  };

  beforeEach(async () => {
    const adminServiceSpy = {
      getDashboardStats: jasmine.createSpy('getDashboardStats').and.returnValue(of(mockStats))
    };

    await TestBed.configureTestingModule({
      imports: [
        AdminDashboardComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        provideRouter([]),
        { provide: AdminService, useValue: adminServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardComponent);
    component = fixture.componentInstance;
    adminService = TestBed.inject(AdminService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty stats object', () => {
    expect(component.stats).toEqual({});
  });

  it('should have lstats defined', () => {
    expect(component.lstats).toBeDefined();
  });

  it('should call loadStats on ngOnInit', () => {
    spyOn(component, 'loadStats');
    
    component.ngOnInit();
    
    expect(component.loadStats).toHaveBeenCalled();
  });

  it('should load stats successfully', () => {
    component.loadStats();
    
    expect(adminService.getDashboardStats).toHaveBeenCalled();
    expect(component.stats).toEqual(mockStats);
  });

  it('should handle error when loading stats', () => {
    spyOn(console, 'error');
    adminService.getDashboardStats.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadStats();
    
    expect(console.error).toHaveBeenCalledWith('Error loading stats:', jasmine.any(Error));
  });

  it('should use fallback stats when service fails', () => {
    adminService.getDashboardStats.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadStats();
    
    expect(component.stats).toBeDefined();
    expect(component.stats.totalCats).toBe(156);
    expect(component.stats.availableCats).toBe(120);
    expect(component.stats.adoptedCats).toBe(89);
    expect(component.stats.totalUsers).toBe(1247);
    expect(component.stats.adminUsers).toBe(5);
    expect(component.stats.totalAdoptions).toBe(234);
    expect(component.stats.pendingAdoptions).toBe(23);
    expect(component.stats.completedAdoptions).toBe(211);
  });

  it('should have correct stats properties for template', () => {
    component.loadStats();
    
    expect(component.stats.totalCats).toBeDefined();
    expect(component.stats.pendingAdoptions).toBeDefined();
    expect(component.stats.totalUsers).toBeDefined();
    expect(component.stats.adoptedCats).toBeDefined();
  });
});