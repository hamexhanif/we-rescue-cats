import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CatSearchComponent } from './cat-search';
import { CatService } from '../../services/cat-service';
import { CatBreed } from '../../models/cat-model';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('CatSearchComponent', () => {
  let component: CatSearchComponent;
  let fixture: ComponentFixture<CatSearchComponent>;
  let catService: any;

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
    const catServiceSpy = {
      getCatBreeds: jasmine.createSpy('getCatBreeds').and.returnValue(of(mockBreeds))
    };

    await TestBed.configureTestingModule({
      imports: [
        CatSearchComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder,
        { provide: CatService, useValue: catServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CatSearchComponent);
    component = fixture.componentInstance;
    catService = TestBed.inject(CatService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty arrays', () => {
    expect(component.allBreeds).toEqual([]);
    expect(component.breedNames).toEqual([]);
  });

  it('should have age ranges defined', () => {
    expect(component.ageRanges).toBeDefined();
    expect(component.ageRanges.length).toBe(4);
    expect(component.ageRanges[0].value).toBe('');
    expect(component.ageRanges[0].label).toBe('Any Age');
  });

  it('should have gender options defined', () => {
    expect(component.genderOptions).toBeDefined();
    expect(component.genderOptions.length).toBe(3);
    expect(component.genderOptions[0].value).toBe('');
    expect(component.genderOptions[0].label).toBe('Any Gender');
  });

  it('should call initializeForm and loadBreeds on ngOnInit', () => {
    spyOn(component, 'initializeForm');
    spyOn(component, 'loadBreeds');
    
    component.ngOnInit();
    
    expect(component.initializeForm).toHaveBeenCalled();
    expect(component.loadBreeds).toHaveBeenCalled();
  });

  describe('initializeForm', () => {
    it('should create form with correct controls', () => {
      component.initializeForm();
      
      expect(component.catSearchForm).toBeDefined();
      expect(component.catSearchForm.get('breed')).toBeTruthy();
      expect(component.catSearchForm.get('ageRange')).toBeTruthy();
      expect(component.catSearchForm.get('gender')).toBeTruthy();
      expect(component.catSearchForm.get('location')).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
      component.initializeForm();
      
      expect(component.catSearchForm.get('breed')?.value).toBe('');
      expect(component.catSearchForm.get('ageRange')?.value).toBe('');
      expect(component.catSearchForm.get('gender')?.value).toBe('');
      expect(component.catSearchForm.get('location')?.value).toBe('');
    });
  });

  describe('loadBreeds', () => {
    it('should load breeds successfully', () => {
      component.loadBreeds();
      
      expect(catService.getCatBreeds).toHaveBeenCalled();
      expect(component.allBreeds).toEqual(mockBreeds);
      expect(component.breedNames).toEqual(['Persian', 'Siamese']);
    });

    it('should handle error and use fallback breeds', () => {
      spyOn(console, 'error');
      catService.getCatBreeds.and.returnValue(throwError(() => new Error('Service error')));
      
      component.loadBreeds();
      
      expect(console.error).toHaveBeenCalledWith('Error loading breeds:', jasmine.any(Error));
      expect(component.breedNames).toEqual([
        'Persian', 'Siamese', 'Maine Coon', 'British Shorthair', 
        'Ragdoll', 'Russian Blue', 'Domestic Shorthair', 'Bengal'
      ]);
    });
  });

  describe('onSearch', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should emit filters when form has values', () => {
      spyOn(component.filtersChanged, 'emit');
      spyOn(console, 'log');
      
      component.catSearchForm.patchValue({
        breed: 'Persian',
        ageRange: '0-2',
        gender: 'FEMALE'
      });
      
      component.onSearch();
      
      expect(console.log).toHaveBeenCalledWith('Emitting filters:', {
        breed: 'Persian',
        ageRange: '0-2',
        gender: 'FEMALE'
      });
      expect(component.filtersChanged.emit).toHaveBeenCalledWith({
        breed: 'Persian',
        ageRange: '0-2',
        gender: 'FEMALE'
      });
    });

    it('should exclude empty values from filters', () => {
      spyOn(component.filtersChanged, 'emit');
      
      component.catSearchForm.patchValue({
        breed: 'Persian',
        ageRange: '',
        gender: 'FEMALE',
        location: '   '
      });
      
      component.onSearch();
      
      expect(component.filtersChanged.emit).toHaveBeenCalledWith({
        breed: 'Persian',
        gender: 'FEMALE'
      });
    });

    it('should emit empty object when no filters', () => {
      spyOn(component.filtersChanged, 'emit');
      
      component.catSearchForm.patchValue({
        breed: '',
        ageRange: '',
        gender: '',
        location: ''
      });
      
      component.onSearch();
      
      expect(component.filtersChanged.emit).toHaveBeenCalledWith({});
    });
  });

  describe('clearFilters', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should reset form and emit empty filters', () => {
      spyOn(component.filtersChanged, 'emit');
      
      // Set some values first
      component.catSearchForm.patchValue({
        breed: 'Persian',
        ageRange: '0-2'
      });
      
      component.clearFilters();
      
      expect(component.catSearchForm.get('breed')?.value).toBeNull();
      expect(component.catSearchForm.get('ageRange')?.value).toBeNull();
      expect(component.filtersChanged.emit).toHaveBeenCalledWith({});
    });
  });

  describe('filter options', () => {
    it('should have correct age range options', () => {
      const expectedAgeRanges = [
        { value: '', label: 'Any Age' },
        { value: '0-2', label: 'Young (0-2 years)' },
        { value: '3-7', label: 'Adult (3-7 years)' },
        { value: '8+', label: 'Senior (8+ years)' }
      ];
      
      expect(component.ageRanges).toEqual(expectedAgeRanges);
    });

    it('should have correct gender options', () => {
      const expectedGenderOptions = [
        { value: '', label: 'Any Gender' },
        { value: 'MALE', label: 'Male' },
        { value: 'FEMALE', label: 'Female' }
      ];
      
      expect(component.genderOptions).toEqual(expectedGenderOptions);
    });
  });
});