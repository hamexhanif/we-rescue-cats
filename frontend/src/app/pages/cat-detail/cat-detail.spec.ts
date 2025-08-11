import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CatDetailComponent } from './cat-detail';
import { CatService } from '../../services/cat-service';
import { AuthService } from '../../services/auth-service';
import { Cat } from '../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CatDetailComponent', () => {
  let component: CatDetailComponent;
  let fixture: ComponentFixture<CatDetailComponent>;
  let catService: jasmine.SpyObj<CatService>;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let dialog: jasmine.SpyObj<MatDialog>;
  let mockActivatedRoute: any;

  const mockCat: Cat = {
    id: 1,
    name: 'Luna',
    age: 2,
    gender: 'FEMALE',
    breedId: 'pers',
    breedName: 'Persian Mix',
    description: 'Sweet and gentle cat looking for a loving home',
    adoptionStatus: 'AVAILABLE',
    latitude: 53.124,
    longitude: 14.223,
    address: 'Dresden',
    imageUrl: 'https://cdn2.thecatapi.com/images/O3btzLlsO.png',
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-15T10:00:00Z'
  };

  beforeEach(async () => {
    const catServiceSpy = jasmine.createSpyObj('CatService', ['getCatById']);
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate'], { url: '/cats/1' });
    const dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    mockActivatedRoute = {
      params: of({ id: '1' }) // Mock route params with cat ID
    };

    await TestBed.configureTestingModule({
      imports: [
        CatDetailComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideRouter([]),
        { provide: CatService, useValue: catServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CatDetailComponent);
    component = fixture.componentInstance;
    catService = TestBed.inject(CatService) as jasmine.SpyObj<CatService>;
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    dialog = TestBed.inject(MatDialog) as jasmine.SpyObj<MatDialog>;

    catService.getCatById.and.returnValue(of(mockCat));
    authService.isAuthenticated.and.returnValue(true);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with loading = true', () => {
    expect(component.loading).toBe(true);
  });

  it('should start with cat = null', () => {
    expect(component.cat).toBe(null);
  });

  it('should call loadCat with correct ID on ngOnInit', () => {
    spyOn(component, 'loadCat');
    
    component.ngOnInit();
    
    expect(component.loadCat).toHaveBeenCalledWith(1);
  });

  it('should load cat successfully', () => {
    component.loadCat(1);
    
    expect(catService.getCatById).toHaveBeenCalledWith(1);
    expect(component.cat).toEqual(mockCat);
    expect(component.loading).toBe(false);
  });

  it('should handle null cat response', () => {
    catService.getCatById.and.returnValue(of(null as any));
    
    component.loadCat(1);
    
    expect(component.cat).toBe(null);
    expect(component.loading).toBe(false);
  });

  it('should handle error when loading cat', () => {
    spyOn(console, 'error');
    catService.getCatById.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadCat(1);
    
    expect(component.loading).toBe(false);
    expect(console.error).toHaveBeenCalledWith('Error loading cat:', jasmine.any(Error));
  });

  it('should use mock cat when service fails', () => {
    catService.getCatById.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadCat(1);
    
    expect(component.cat).toBeDefined();
    expect(component.cat?.id).toBe(1);
    expect(component.cat?.name).toBe('Luna');
  });

  describe('openAdoptionForm', () => {
    beforeEach(() => {
      component.cat = mockCat;
    });

    it('should open adoption dialog when user is authenticated', () => {
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);
      
      component.openAdoptionForm();
      
      expect(authService.isAuthenticated).toHaveBeenCalled();
      expect(dialog.open).toHaveBeenCalled();
    });

    it('should navigate to login when user is not authenticated', () => {
      authService.isAuthenticated.and.returnValue(false);
      
      component.openAdoptionForm();
      
      expect(authService.isAuthenticated).toHaveBeenCalled();
      expect(router.navigate).toHaveBeenCalledWith(['/login'], { 
        queryParams: { returnUrl: '/cats/1' } 
      });
      expect(dialog.open).not.toHaveBeenCalled();
    });

    it('should handle dialog result when adoption form is submitted', () => {
      spyOn(console, 'log');
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      const mockResult = { name: 'John Doe', email: 'john@example.com' };
      dialogRefSpy.afterClosed.and.returnValue(of(mockResult));
      dialog.open.and.returnValue(dialogRefSpy);
      
      component.openAdoptionForm();
      
      expect(console.log).toHaveBeenCalledWith('Adoption application submitted:', mockResult);
    });

    it('should handle dialog cancellation', () => {
      spyOn(console, 'log');
      const dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['afterClosed']);
      dialogRefSpy.afterClosed.and.returnValue(of(null));
      dialog.open.and.returnValue(dialogRefSpy);
      
      component.openAdoptionForm();
      
      expect(console.log).not.toHaveBeenCalled();
    });
  });

  describe('getMockCat', () => {
    it('should return a mock cat with correct properties', () => {
      const result = (component as any).getMockCat(5);
      
      expect(result).toBeDefined();
      expect(result.id).toBe(1);
      expect(result.name).toBe('Luna');
      expect(result.adoptionStatus).toBe('AVAILABLE');
      expect(result.breedName).toBeDefined();
      expect(result.description).toBeDefined();
    });
  });

  describe('route params handling', () => {
    it('should handle string ID from route params', () => {
      mockActivatedRoute.params = of({ id: '123' });
      spyOn(component, 'loadCat');
      
      component.ngOnInit();
      
      expect(component.loadCat).toHaveBeenCalledWith(123);
    });

    it('should handle invalid ID from route params', () => {
      mockActivatedRoute.params = of({ id: 'invalid' });
      spyOn(component, 'loadCat');
      
      component.ngOnInit();
      
      expect(component.loadCat).toHaveBeenCalledWith(NaN);
    });
  });
});