import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreateCatDialogComponent } from './create-cat-form';
import { AdminService } from '../../services/admin-service';
import { CatService } from '../../services/cat-service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CatBreed } from '../../models/cat-model';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('CreateCatDialogComponent', () => {
  let component: CreateCatDialogComponent;
  let fixture: ComponentFixture<CreateCatDialogComponent>;
  let adminService: any;
  let catService: any;
  let dialogRef: any;
  let snackBar: any;

  const mockBreeds: CatBreed[] = [
    {
      id: 'pers',
      name: 'Persian',
      description: 'Persian cat breed',
      origin: 'Iran',
      childFriendly: 4,
      dogFriendly: 3,
      energyLevel: 2,
      grooming: 5,
      healthIssues: 3,
      intelligence: 3,
      socialNeeds: 4,
      strangerFriendly: 2,
      adaptability: 4,
      affectionLevel: 2,
      wikipediaUrl: 'url',
      referenceImageId: 'ref-id',
      imageUrl: 'breed-image.jpg'
    },
    {
      id: 'siam',
      name: 'Siamese',
      description: 'Siamese cat breed',
      origin: 'Thailand',
      childFriendly: 4,
      dogFriendly: 4,
      energyLevel: 5,
      grooming: 2,
      healthIssues: 2,
      intelligence: 5,
      socialNeeds: 5,
      strangerFriendly: 4,
      adaptability: 3,
      affectionLevel: 2,
      wikipediaUrl: 'url',
      referenceImageId: 'ref-id',
      imageUrl: 'breed-image.jpg'
    }
  ];

  beforeEach(async () => {
    const adminServiceSpy = {
      createCat: jasmine.createSpy('createCat').and.returnValue(of({ success: true }))
    };
    
    const catServiceSpy = {
      getCatBreeds: jasmine.createSpy('getCatBreeds').and.returnValue(of(mockBreeds))
    };
    
    const dialogRefSpy = {
      close: jasmine.createSpy('close')
    };
    
    const snackBarSpy = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [
        CreateCatDialogComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: CatService, useValue: catServiceSpy },
        { provide: MatDialogRef, useValue: dialogRefSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateCatDialogComponent);
    component = fixture.componentInstance;
    adminService = TestBed.inject(AdminService);
    catService = TestBed.inject(CatService);
    dialogRef = TestBed.inject(MatDialogRef);
    snackBar = TestBed.inject(MatSnackBar);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with isSubmitting = false', () => {
    expect(component.isSubmitting).toBe(false);
  });

  it('should start with empty breeds array', () => {
    expect(component.breeds).toEqual([]);
  });

  it('should have form defined', () => {
    expect(component.catForm).toBeDefined();
  });

  it('should call loadBreeds on ngOnInit', () => {
    spyOn(component, 'loadBreeds');
    
    component.ngOnInit();
    
    expect(component.loadBreeds).toHaveBeenCalled();
  });

  describe('Form Structure', () => {
    it('should have all required form controls', () => {
      expect(component.catForm.get('name')).toBeTruthy();
      expect(component.catForm.get('age')).toBeTruthy();
      expect(component.catForm.get('gender')).toBeTruthy();
      expect(component.catForm.get('description')).toBeTruthy();
      expect(component.catForm.get('breed')).toBeTruthy();
      expect(component.catForm.get('breedId')).toBeTruthy();
      expect(component.catForm.get('breedName')).toBeTruthy();
      expect(component.catForm.get('imageUrl')).toBeTruthy();
      expect(component.catForm.get('latitude')).toBeTruthy();
      expect(component.catForm.get('longitude')).toBeTruthy();
      expect(component.catForm.get('address')).toBeTruthy();
    });

    it('should have correct validators', () => {
      const form = component.catForm;
      
      expect(form.get('name')?.hasError('required')).toBe(true);
      expect(form.get('age')?.hasError('required')).toBe(true);
      expect(form.get('gender')?.hasError('required')).toBe(true);
      expect(form.get('description')?.hasError('required')).toBe(true);
      expect(form.get('breed')?.hasError('required')).toBe(true);
      expect(form.get('latitude')?.hasError('required')).toBe(true);
      expect(form.get('longitude')?.hasError('required')).toBe(true);
      expect(form.get('address')?.hasError('required')).toBe(true);
    });

    it('should validate age range', () => {
      const ageControl = component.catForm.get('age');
      
      ageControl?.setValue(-1);
      expect(ageControl?.hasError('min')).toBe(true);
      
      ageControl?.setValue(30);
      expect(ageControl?.hasError('max')).toBe(true);
      
      ageControl?.setValue(5);
      expect(ageControl?.hasError('min')).toBe(false);
      expect(ageControl?.hasError('max')).toBe(false);
    });

    it('should validate name minimum length', () => {
      const nameControl = component.catForm.get('name');
      
      // Test with empty name (minLength = 1)
      nameControl?.setValue('');
      nameControl?.markAsTouched();
      expect(nameControl?.hasError('required')).toBe(true);
      
      // Test with valid name
      nameControl?.setValue('Luna');
      expect(nameControl?.hasError('required')).toBe(false);
      expect(nameControl?.hasError('minlength')).toBe(false);
    });
  });

  describe('loadBreeds', () => {
    it('should load breeds successfully', () => {
      spyOn(console, 'log');
      
      component.loadBreeds();
      
      expect(catService.getCatBreeds).toHaveBeenCalled();
      expect(component.breeds).toEqual(mockBreeds);
      expect(console.log).toHaveBeenCalledWith('Loaded breeds:', mockBreeds);
    });

    it('should handle error when loading breeds', () => {
      spyOn(console, 'error');
      catService.getCatBreeds.and.returnValue(throwError(() => new Error('Service error')));
      
      component.loadBreeds();
      
      expect(console.error).toHaveBeenCalledWith('Error loading breeds:', jasmine.any(Error));
      expect(snackBar.open).toHaveBeenCalledWith('Error loading breeds', 'Close', { duration: 3000 });
    });
  });

  describe('onBreedSelected', () => {
    it('should update form when breed is selected', () => {
      const mockEvent = {
        option: {
          value: mockBreeds[0]
        }
      };
      
      component.onBreedSelected(mockEvent);
      
      expect(component.catForm.get('breed')?.value).toBe('Persian');
      expect(component.catForm.get('breedId')?.value).toBe('pers');
      expect(component.catForm.get('breedName')?.value).toBe('Persian');
    });
  });

  describe('displayBreed', () => {
    it('should return breed name for breed object', () => {
      const result = component.displayBreed(mockBreeds[0]);
      expect(result).toBe('Persian');
    });

    it('should return string as is', () => {
      const result = component.displayBreed('Test String');
      expect(result).toBe('Test String');
    });

    it('should return empty string for null/undefined', () => {
      expect(component.displayBreed(null as any)).toBe('');
      expect(component.displayBreed(undefined as any)).toBe('');
    });
  });

  describe('Form Submission', () => {
    beforeEach(() => {
      // Fill form with valid data
      component.catForm.patchValue({
        name: 'Luna',
        age: 2,
        gender: 'FEMALE',
        description: 'Sweet cat',
        breed: 'Persian',
        breedId: 'pers',
        breedName: 'Persian',
        imageUrl: 'test-image.jpg',
        latitude: 51.0504,
        longitude: 13.7373,
        address: 'Dresden, Germany'
      });
    });

    it('should not submit invalid form', () => {
      component.catForm.patchValue({ name: '' }); // Make form invalid
      spyOn(component as any, 'markFormGroupTouched');
      spyOn(console, 'log');
      
      component.onSubmit();
      
      expect(adminService.createCat).not.toHaveBeenCalled();
      expect(console.log).toHaveBeenCalledWith('Form is invalid:', null);
      expect((component as any).markFormGroupTouched).toHaveBeenCalled();
    });

    it('should call createCat with correct data when form is valid', () => {
      // Ensure form is valid by checking required fields
      expect(component.catForm.valid).toBe(true);
      
      component.onSubmit();
      
      expect(adminService.createCat).toHaveBeenCalledWith({
        name: 'Luna',
        age: 2,
        gender: 'FEMALE',
        description: 'Sweet cat',
        breedId: 'pers',
        breedName: 'Persian',
        imageUrl: 'test-image.jpg',
        latitude: 51.0504,
        longitude: 13.7373,
        address: 'Dresden, Germany',
        status: 'AVAILABLE'
      });
    });
  });

  describe('onCancel', () => {
    it('should close dialog with false', () => {
      component.onCancel();
      
      expect(dialogRef.close).toHaveBeenCalledWith(false);
    });
  });

  describe('markFormGroupTouched', () => {
    it('should mark all form controls as touched', () => {
      (component as any).markFormGroupTouched();
      
      Object.keys(component.catForm.controls).forEach(key => {
        expect(component.catForm.get(key)?.touched).toBe(true);
      });
    });
  });

  describe('filteredBreeds observable', () => {
    it('should have filteredBreeds observable defined', () => {
      expect(component.filteredBreeds).toBeDefined();
    });
  });
});