import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdminCatsComponent } from './admin-cats';
import { CatService } from '../../../services/cat-service';
import { AdminService } from '../../../services/admin-service';
import { AuthService } from '../../../services/auth-service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Cat } from '../../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('AdminCatsComponent', () => {
  let component: AdminCatsComponent;
  let fixture: ComponentFixture<AdminCatsComponent>;
  let catService: any;
  let adminService: any;
  let authService: any;
  let dialog: any;
  let snackBar: any;

  const mockCats: Cat[] = [
    {
      id: 1,
      name: 'Luna',
      age: 2,
      gender: 'FEMALE',
      breedId: 'pers',
      breedName: 'Persian Mix',
      description: 'Sweet and gentle cat',
      adoptionStatus: 'AVAILABLE',
      latitude: 53.124,
      longitude: 14.223,
      address: 'Dresden',
      imageUrl: 'https://example.com/luna.jpg',
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    },
    {
      id: 2,
      name: 'Max',
      age: 5,
      gender: 'MALE',
      breedId: 'siam',
      breedName: 'Siamese',
      description: 'Playful and energetic cat',
      adoptionStatus: 'ADOPTED',
      latitude: 53.124,
      longitude: 14.223,
      address: 'Dresden',
      imageUrl: 'https://example.com/max.jpg',
      createdAt: '2024-01-10T10:00:00Z',
      updatedAt: '2024-01-10T10:00:00Z'
    }
  ];

  beforeEach(async () => {
    const catServiceSpy = {
      getAllCats: jasmine.createSpy('getAllCats').and.returnValue(of(mockCats))
    };
    
    const adminServiceSpy = {
      deleteCat: jasmine.createSpy('deleteCat').and.returnValue(of({ success: true }))
    };
    
    const authServiceSpy = {
      isAuthenticated: jasmine.createSpy('isAuthenticated').and.returnValue(true),
      isAdmin: jasmine.createSpy('isAdmin').and.returnValue(true),
      getCurrentUser: jasmine.createSpy('getCurrentUser').and.returnValue({ id: 1, role: 'ADMIN' }),
      getToken: jasmine.createSpy('getToken').and.returnValue('fake-token')
    };
    
    const dialogSpy = {
      open: jasmine.createSpy('open').and.returnValue({
        afterClosed: () => of(true)
      })
    };
    
    const snackBarSpy = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [
        AdminCatsComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: CatService, useValue: catServiceSpy },
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatDialog, useValue: dialogSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminCatsComponent);
    component = fixture.componentInstance;
    catService = TestBed.inject(CatService);
    adminService = TestBed.inject(AdminService);
    authService = TestBed.inject(AuthService);
    dialog = TestBed.inject(MatDialog);
    snackBar = TestBed.inject(MatSnackBar);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty cats array', () => {
    expect(component.cats).toEqual([]);
  });

  it('should have correct displayedColumns', () => {
    expect(component.displayedColumns).toEqual(['image', 'name', 'details', 'status', 'location', 'actions']);
  });

  it('should call loadCats on ngOnInit', () => {
    spyOn(component, 'loadCats');
    
    component.ngOnInit();
    
    expect(component.loadCats).toHaveBeenCalled();
  });

  it('should load cats successfully', () => {
    component.loadCats();
    
    expect(catService.getAllCats).toHaveBeenCalled();
    expect(component.cats).toEqual(mockCats);
  });

  it('should handle null cats response', () => {
    catService.getAllCats.and.returnValue(of(null));
    
    component.loadCats();
    
    expect(component.cats).toEqual([]);
  });

  it('should handle error when loading cats', () => {
    spyOn(console, 'error');
    catService.getAllCats.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadCats();
    
    expect(console.error).toHaveBeenCalledWith('Error loading cats:', jasmine.any(Error));
    expect(snackBar.open).toHaveBeenCalledWith('Error loading cats', 'Close', { duration: 3000 });
  });

  describe('addNewCat', () => {
    it('should open create cat dialog', () => {
      component.addNewCat();
      
      expect(dialog.open).toHaveBeenCalledWith(
        jasmine.any(Function),
        {
          width: '600px',
          maxHeight: '90vh',
          disableClose: false,
          autoFocus: true
        }
      );
    });

    it('should reload cats when dialog returns true', () => {
      spyOn(component, 'loadCats');
      
      component.addNewCat();
      
      expect(component.loadCats).toHaveBeenCalled();
    });

    it('should not reload cats when dialog is cancelled', () => {
      dialog.open.and.returnValue({
        afterClosed: () => of(false)
      });
      spyOn(component, 'loadCats');
      
      component.addNewCat();
      
      expect(component.loadCats).not.toHaveBeenCalled();
    });
  });

  describe('deleteCat', () => {
    const testCat = mockCats[0];

    it('should delete cat when confirmed', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(component, 'loadCats');
      
      component.deleteCat(testCat);
      
      expect(window.confirm).toHaveBeenCalledWith('Are you sure you want to delete Luna?');
      expect(adminService.deleteCat).toHaveBeenCalledWith(1);
      expect(snackBar.open).toHaveBeenCalledWith('Cat deleted successfully', 'Close', { duration: 3000 });
      expect(component.loadCats).toHaveBeenCalled();
    });

    it('should not delete when user cancels confirmation', () => {
      spyOn(window, 'confirm').and.returnValue(false);
      
      component.deleteCat(testCat);
      
      expect(window.confirm).toHaveBeenCalled();
      expect(adminService.deleteCat).not.toHaveBeenCalled();
    });

    it('should handle delete error', () => {
      spyOn(window, 'confirm').and.returnValue(true);
      spyOn(console, 'error');
      adminService.deleteCat.and.returnValue(throwError(() => new Error('Delete failed')));
      
      component.deleteCat(testCat);
      
      expect(console.error).toHaveBeenCalledWith('Error deleting cat:', jasmine.any(Error));
      expect(snackBar.open).toHaveBeenCalledWith('Error deleting cat', 'Close', { duration: 3000 });
    });
  });
});