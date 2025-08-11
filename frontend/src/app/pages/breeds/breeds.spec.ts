import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { BreedsComponent } from './breeds';
import { CatService } from '../../services/cat-service';
import { CatBreed } from '../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('BreedsComponent', () => {
  let component: BreedsComponent;
  let fixture: ComponentFixture<BreedsComponent>;
  let catService: jasmine.SpyObj<CatService>;

  const mockBreeds: CatBreed[] = [
    {
      id: 'pers',
      name: 'Persian',
      description: 'The Persian cat is a long-haired breed of cat characterized by its round face and short muzzle.',
      origin: 'Iran (Persia)',
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
      referenceImageId: 'adadasdasd',
      imageUrl: 'https://cdn2.thecatapi.com/images/O3btzLlsO.png'
    },
    {
      id: 'siam',
      name: 'Siamese',
      description: 'The Siamese cat is one of the first distinctly recognized breeds of Oriental cat.',
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
      referenceImageId: 'adadasdasd',
      imageUrl: 'https://cdn2.thecatapi.com/images/O3btzLlsO.png'
    },
    {
      id: 'mcoo',
      name: 'Maine Coon',
      description: 'The Maine Coon is a large domesticated cat breed.',
      origin: 'United States',
      childFriendly: 5,
      dogFriendly: 5,
      energyLevel: 3,
      grooming: 3,
      healthIssues: 2,
      intelligence: 4,
      socialNeeds: 3,
      strangerFriendly: 3,
      adaptability: 4,
      affectionLevel: 4,
      wikipediaUrl: 'url',
      referenceImageId: 'adadasdasd',
      imageUrl: 'https://cdn2.thecatapi.com/images/O3btzLlsO.png'
    }
  ];

  beforeEach(async () => {
    const catServiceSpy = jasmine.createSpyObj('CatService', ['getCatBreeds']);

    await TestBed.configureTestingModule({
      imports: [
        BreedsComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideRouter([]),
        { provide: CatService, useValue: catServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(BreedsComponent);
    component = fixture.componentInstance;
    catService = TestBed.inject(CatService) as jasmine.SpyObj<CatService>;

    // Set up default returns
    catService.getCatBreeds.and.returnValue(of(mockBreeds));
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with loading = true', () => {
    expect(component.loading).toBe(true);
  });

  it('should start with empty breeds array', () => {
    expect(component.breeds).toEqual([]);
  });

  it('should start with empty allBreeds array', () => {
    expect(component.allBreeds).toEqual([]);
  });

  it('should call loadBreeds on ngOnInit', () => {
    spyOn(component, 'loadBreeds');
    
    component.ngOnInit();
    
    expect(component.loadBreeds).toHaveBeenCalled();
  });

  it('should call CatService.getCatBreeds when loadBreeds runs', () => {
    component.loadBreeds();
    
    expect(catService.getCatBreeds).toHaveBeenCalled();
  });

  it('should set loading to false after loadBreeds', () => {
    component.loadBreeds();
    
    expect(component.loading).toBe(false);
  });

  it('should populate breeds and allBreeds after successful load', () => {
    component.loadBreeds();
    
    expect(component.allBreeds).toEqual(mockBreeds);
    expect(component.breeds).toEqual(mockBreeds);
  });

  it('should handle error when loading breeds', () => {
    spyOn(console, 'error');
    catService.getCatBreeds.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadBreeds();
    
    expect(component.loading).toBe(false);
    expect(console.error).toHaveBeenCalledWith('Error loading breeds:', jasmine.any(Error));
  });

  it('should use mock breeds when service fails', () => {
    catService.getCatBreeds.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadBreeds();
    
    expect(component.allBreeds).toBeDefined();
    expect(component.breeds).toBeDefined();
    expect(component.allBreeds.length).toBeGreaterThan(0);
    expect(component.breeds.length).toBeGreaterThan(0);
  });

  it('should update breeds when onBreedsFiltered is called', () => {
    const filteredBreeds = [mockBreeds[0]]; // Just Persian
    
    component.onBreedsFiltered(filteredBreeds);
    
    expect(component.breeds).toEqual(filteredBreeds);
  });

  it('should handle empty filtered breeds', () => {
    component.onBreedsFiltered([]);
    
    expect(component.breeds).toEqual([]);
  });
});