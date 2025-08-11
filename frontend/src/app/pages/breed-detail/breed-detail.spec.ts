import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { BreedDetailComponent } from './breed-detail';
import { CatService } from '../../services/cat-service';
import { CatBreed, Cat } from '../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('BreedDetailComponent', () => {
  let component: BreedDetailComponent;
  let fixture: ComponentFixture<BreedDetailComponent>;
  let catService: jasmine.SpyObj<CatService>;
  let mockActivatedRoute: any;

  const mockBreed: CatBreed = {
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
  };

  const mockBreeds: CatBreed[] = [
    mockBreed,
    {
      id: 'siam',
      name: 'Siamese',
      description: 'The Siamese cat is one of the first distinctly recognized breeds.',
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
    }
  ];

  beforeEach(async () => {
    const catServiceSpy = jasmine.createSpyObj('CatService', ['getCatBreeds']);
    
    mockActivatedRoute = {
      params: of({ id: 'pers' })
    };

    await TestBed.configureTestingModule({
      imports: [
        BreedDetailComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideRouter([]),
        { provide: CatService, useValue: catServiceSpy },
        { provide: ActivatedRoute, useValue: mockActivatedRoute }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(BreedDetailComponent);
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

  it('should start with breed = null', () => {
    expect(component.breed).toBe(null);
  });

  it('should start with empty availableCats array', () => {
    expect(component.availableCats).toEqual([]);
  });

  it('should call loadBreed with correct ID on ngOnInit', () => {
    spyOn(component, 'loadBreed');
    
    component.ngOnInit();
    
    expect(component.loadBreed).toHaveBeenCalledWith('pers');
  });

  it('should load breed successfully', () => {
    component.loadBreed('pers');
    
    expect(catService.getCatBreeds).toHaveBeenCalled();
    expect(component.breed).toEqual(mockBreed);
    expect(component.loading).toBe(false);
  });

  it('should handle breed not found', () => {
    component.loadBreed('unknown');
    
    expect(component.breed).toBe(null);
    expect(component.loading).toBe(false);
  });

  it('should handle error when loading breeds', () => {
    spyOn(console, 'error');
    catService.getCatBreeds.and.returnValue(throwError(() => new Error('Service error')));
    
    component.loadBreed('pers');
    
    expect(component.loading).toBe(false);
    expect(console.error).toHaveBeenCalledWith('Error loading breed:', jasmine.any(Error));
  });

  describe('Characteristic descriptions', () => {
    beforeEach(() => {
      component.breed = mockBreed;
    });

    it('should return correct child friendly description', () => {
      const result = component.getChildFriendlyDescription(4);
      expect(result).toContain('Great with children');
    });

    it('should return correct dog friendly description', () => {
      const result = component.getDogFriendlyDescription(3);
      expect(result).toContain('Generally gets along with dogs');
    });

    it('should return correct energy level description', () => {
      const result = component.getEnergyLevelDescription(2);
      expect(result).toContain('Prefers quiet activities');
    });

    it('should return correct grooming description', () => {
      const result = component.getGroomingDescription(5);
      expect(result).toContain('Daily grooming essential');
    });

    it('should return correct health description', () => {
      const result = component.getHealthDescription(3);
      expect(result).toContain('Some breed-specific health concerns');
    });

    it('should return correct intelligence description', () => {
      const result = component.getIntelligenceDescription(3);
      expect(result).toContain('Average intelligence');
    });

    it('should return correct social needs description', () => {
      const result = component.getSocialNeedsDescription(4);
      expect(result).toContain('Thrives on social interaction');
    });

    it('should return correct stranger friendly description', () => {
      const result = component.getStrangerFriendlyDescription(2);
      expect(result).toContain('Cautious but may warm up');
    });

    it('should return correct adaptability description', () => {
      const result = component.getAdaptabilityDescription(4);
      expect(result).toContain('Quite adaptable');
    });

    it('should return correct affection level description', () => {
      const result = component.getAffectionLevelDescription(2);
      expect(result).toContain('Occasionally affectionate');
    });

    it('should handle invalid rating gracefully', () => {
      const result = component.getChildFriendlyDescription(0);
      expect(result).toContain('Good with well-behaved children');
    });

    it('should handle rating above range gracefully', () => {
      const result = component.getEnergyLevelDescription(10);
      expect(result).toContain('Moderate energy');
    });
  });

  describe('Care tips', () => {
    beforeEach(() => {
      component.breed = mockBreed;
    });

    it('should return feeding tips for high energy cats', () => {
      component.breed!.energyLevel = 5;
      const result = component.getFeedingTips();
      expect(result).toContain('High-energy breeds need protein-rich diets');
    });

    it('should return feeding tips for low energy cats', () => {
      component.breed!.energyLevel = 1;
      const result = component.getFeedingTips();
      expect(result).toContain('Lower energy breeds are prone to weight gain');
    });

    it('should return general feeding tips for moderate energy cats', () => {
      component.breed!.energyLevel = 3;
      const result = component.getFeedingTips();
      expect(result).toContain('Provide high-quality cat food');
    });

    it('should return grooming tips for high maintenance breeds', () => {
      component.breed!.grooming = 5;
      const result = component.getGroomingTips();
      expect(result).toContain('Daily brushing is essential');
    });

    it('should return grooming tips for low maintenance breeds', () => {
      component.breed!.grooming = 1;
      const result = component.getGroomingTips();
      expect(result).toContain('Minimal grooming needed');
    });

    it('should return exercise tips for high energy breeds', () => {
      component.breed!.energyLevel = 5;
      const result = component.getExerciseTips();
      expect(result).toContain('Needs multiple active play sessions');
    });

    it('should return exercise tips for low energy breeds', () => {
      component.breed!.energyLevel = 1;
      const result = component.getExerciseTips();
      expect(result).toContain('Low exercise needs');
    });

    it('should return health tips for high health issue breeds', () => {
      component.breed!.healthIssues = 5;
      const result = component.getHealthTips();
      expect(result).toContain('Regular veterinary check-ups are crucial');
    });

    it('should return health tips for healthy breeds', () => {
      component.breed!.healthIssues = 1;
      const result = component.getHealthTips();
      expect(result).toContain('Generally healthy breed');
    });

    it('should handle null breed gracefully', () => {
      component.breed = null;
      expect(component.getFeedingTips()).toBe('');
      expect(component.getGroomingTips()).toBe('');
      expect(component.getExerciseTips()).toBe('');
      expect(component.getHealthTips()).toBe('');
    });
  });

  describe('route params handling', () => {
    it('should handle different breed IDs from route params', () => {
      mockActivatedRoute.params = of({ id: 'siam' });
      spyOn(component, 'loadBreed');
      
      component.ngOnInit();
      
      expect(component.loadBreed).toHaveBeenCalledWith('siam');
    });
  });
});