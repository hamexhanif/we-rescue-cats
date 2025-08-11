import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AdoptionFormComponent } from './adoption-form';
import { CatService } from '../../services/cat-service';
import { AuthService } from '../../services/auth-service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Cat } from '../../models/cat-model';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('AdoptionFormComponent', () => {
  let component: AdoptionFormComponent;
  let fixture: ComponentFixture<AdoptionFormComponent>;
  let catService: any;
  let authService: any;
  let dialogRef: any;
  let snackBar: any;

  const mockCat: Cat = {
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
    imageUrl: 'luna.jpg',
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-15T10:00:00Z'
  };

  const mockUser = {
    id: 1,
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@example.com'
  };

  beforeEach(async () => {
    const catServiceSpy = {
      submitApplication: jasmine.createSpy('submitApplication').and.returnValue(of({ success: true }))
    };
    
    const authServiceSpy = {
      getToken: jasmine.createSpy('getToken').and.returnValue('fake-token'),
      getCurrentUser: jasmine.createSpy('getCurrentUser').and.returnValue(mockUser)
    };
    
    const dialogRefSpy = {
      close: jasmine.createSpy('close')
    };
    
    const snackBarSpy = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [
        AdoptionFormComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: CatService, useValue: catServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: MAT_DIALOG_DATA, useValue: { cat: mockCat } }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AdoptionFormComponent);
    component = fixture.componentInstance;
    catService = TestBed.inject(CatService);
    authService = TestBed.inject(AuthService);
    dialogRef = TestBed.inject(MatDialogRef);
    snackBar = TestBed.inject(MatSnackBar);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with loading = false', () => {
    expect(component.loading).toBe(false);
  });

  it('should receive cat data from dialog injection', () => {
    expect(component.data).toBeDefined();
    expect(component.data.cat).toEqual(mockCat);
  });

  it('should call initializeForm on ngOnInit', () => {
    spyOn(component, 'initializeForm');
    
    component.ngOnInit();
    
    expect(component.initializeForm).toHaveBeenCalled();
  });

  describe('initializeForm', () => {
    it('should create form with correct controls', () => {
      component.initializeForm();
      
      expect(component.adoptionForm).toBeDefined();
      expect(component.adoptionForm.get('experienceWithCats')).toBeTruthy();
      expect(component.adoptionForm.get('reasonForAdoption')).toBeTruthy();
    });

    it('should initialize form with empty values and required validators', () => {
      component.initializeForm();
      
      expect(component.adoptionForm.get('experienceWithCats')?.value).toBe('');
      expect(component.adoptionForm.get('reasonForAdoption')?.value).toBe('');
      
      expect(component.adoptionForm.get('experienceWithCats')?.hasError('required')).toBe(true);
      expect(component.adoptionForm.get('reasonForAdoption')?.hasError('required')).toBe(true);
    });
  });

  describe('onSubmit', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should not submit when form is invalid', () => {
      component.onSubmit();
      
      expect(catService.submitApplication).not.toHaveBeenCalled();
    });

    it('should submit application when form is valid and user is logged in', () => {
      component.adoptionForm.patchValue({
        experienceWithCats: 'I have had cats for 10 years',
        reasonForAdoption: 'I love cats and want to give Luna a good home'
      });
      
      component.onSubmit();
      
      expect(catService.submitApplication).toHaveBeenCalledWith({
        userId: mockUser.id,
        catId: mockCat.id,
        notes: 'Experience with Cats: I have had cats for 10 years \nReason for Adoption: I love cats and want to give Luna a good home'
      });
    });
  });

  describe('cancel', () => {
    it('should close dialog without data', () => {
      component.cancel();
      
      expect(dialogRef.close).toHaveBeenCalledWith();
    });
  });

  describe('form validation', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should be invalid when empty', () => {
      expect(component.adoptionForm.valid).toBe(false);
    });

    it('should be valid when both fields are filled', () => {
      component.adoptionForm.patchValue({
        experienceWithCats: 'I have experience',
        reasonForAdoption: 'I want to adopt'
      });
      
      expect(component.adoptionForm.valid).toBe(true);
    });

    it('should be invalid when only one field is filled', () => {
      component.adoptionForm.patchValue({
        experienceWithCats: 'I have experience'
      });
      
      expect(component.adoptionForm.valid).toBe(false);
    });
  });
});