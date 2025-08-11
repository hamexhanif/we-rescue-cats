import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ProfileComponent } from './profile';
import { AuthService } from '../../services/auth-service';
import { CatService } from '../../services/cat-service';
import { User } from '../../models/auth-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  let authService: jasmine.SpyObj<AuthService>;
  let catService: jasmine.SpyObj<CatService>;

  const mockUser: User = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com',
    streetAddress: '123 Main St',
    postalCode: '12345',
    role: 'USER',
    createdAt: '2024-01-01T00:00:00Z',
    enabled: true,
    lastLogin: '2024-01-01T00:00:00Z'
  };

  const mockApplications = [
    {
      id: 1,
      cat: {
        id: 1,
        name: 'Luna',
        age: 2,
        gender: 'FEMALE',
        breedName: 'Persian Mix'
      },
      adoptionDate: '2024-01-15T10:00:00Z',
      status: 'PENDING',
      adminNotes: ''
    },
    {
      id: 2,
      cat: {
        id: 2,
        name: 'Max',
        age: 3,
        gender: 'MALE',
        breedName: 'Siamese'
      },
      adoptionDate: '2024-01-10T10:00:00Z',
      status: 'APPROVED',
      adminNotes: 'Great match!'
    }
  ];

  beforeEach(async () => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentUser'], {
      currentUser$: of(mockUser)
    });
    const catServiceSpy = jasmine.createSpyObj('CatService', ['getUserAdoptions']);

    await TestBed.configureTestingModule({
      imports: [
        ProfileComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: CatService, useValue: catServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    catService = TestBed.inject(CatService) as jasmine.SpyObj<CatService>;

    authService.getCurrentUser.and.returnValue(mockUser);
    catService.getUserAdoptions.and.returnValue(of(mockApplications));
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize currentUser$ from AuthService', () => {
    expect(component.currentUser$).toBeDefined();
  });

  it('should start with empty applications array', () => {
    expect(component.applications).toEqual([]);
  });

  it('should have correct displayedColumns for table', () => {
    const expectedColumns = ['catName', 'adoptionDate', 'status', 'adminNotes'];
    expect(component.displayedColumns).toEqual(expectedColumns);
  });

  it('should call loadApplications on ngOnInit', () => {
    spyOn(component, 'loadApplications');
    
    component.ngOnInit();
    
    expect(component.loadApplications).toHaveBeenCalled();
  });

  it('should load applications when user exists', () => {
    component.loadApplications();
    
    expect(authService.getCurrentUser).toHaveBeenCalled();
    expect(catService.getUserAdoptions).toHaveBeenCalledWith(mockUser.id);
    expect(component.applications).toEqual(mockApplications);
  });

  it('should not call getUserAdoptions when no user', () => {
    authService.getCurrentUser.and.returnValue(null);
    
    component.loadApplications();
    
    expect(catService.getUserAdoptions).not.toHaveBeenCalled();
  });

  it('should handle error when loading applications', () => {
    spyOn(console, 'error');
    catService.getUserAdoptions.and.returnValue(throwError(() => new Error('Error loading applications')));
    
    component.loadApplications();
    
    expect(console.error).toHaveBeenCalledWith('Error loading applications:', jasmine.any(Error));
  });

  it('should get currentUser$ as Observable', () => {
    component.currentUser$.subscribe(user => {
      expect(user).toEqual(mockUser);
    });
  });
});