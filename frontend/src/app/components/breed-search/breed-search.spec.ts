import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BreedSearchComponent } from './breed-search';
import { CatBreed } from '../../models/cat-model';
import { FormBuilder } from '@angular/forms';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('BreedSearchComponent', () => {
  let component: BreedSearchComponent;
  let fixture: ComponentFixture<BreedSearchComponent>;

  const mockBreeds: CatBreed[] = [
    {
      id: 'pers',
      name: 'Persian',
      description: 'Persian cat breed',
      origin: 'Iran',
      childFriendly: 5,
      dogFriendly: 2,
      energyLevel: 1,
      grooming: 5,
      healthIssues: 4,
      intelligence: 3,
      socialNeeds: 4,
      strangerFriendly: 2,
      adaptability: 4,
      affectionLevel: 5,
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
      affectionLevel: 4,
      wikipediaUrl: 'url',
      referenceImageId: 'ref-id',
      imageUrl: 'breed-image.jpg'
    },
    {
      id: 'ragd',
      name: 'Ragdoll',
      description: 'Ragdoll cat breed',
      origin: 'USA',
      childFriendly: 5,
      dogFriendly: 4,
      energyLevel: 2,
      grooming: 4,
      healthIssues: 3,
      intelligence: 3,
      socialNeeds: 4,
      strangerFriendly: 3,
      adaptability: 4,
      affectionLevel: 5,
      wikipediaUrl: 'url',
      referenceImageId: 'ref-id',
      imageUrl: 'breed-image.jpg'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        BreedSearchComponent,
        NoopAnimationsModule
      ],
      providers: [
        FormBuilder
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(BreedSearchComponent);
    component = fixture.componentInstance;
    component.allBreeds = mockBreeds;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty allBreeds input', () => {
    const newComponent = TestBed.createComponent(BreedSearchComponent).componentInstance;
    expect(newComponent.allBreeds).toEqual([]);
  });

  it('should have characteristic options defined', () => {
    expect(component.characteristicOptions).toBeDefined();
    expect(component.characteristicOptions.length).toBe(10);
    
    const expectedOptions = [
      'Child Friendly', 'Dog Friendly', 'High Energy', 'High Grooming Needs',
      'High Intelligence', 'High Social Needs', 'Stranger Friendly', 
      'Highly Adaptable', 'Very Affectionate', 'Low Health Issues'
    ];
    
    const actualLabels = component.characteristicOptions.map(opt => opt.label);
    expect(actualLabels).toEqual(expectedOptions);
  });

  it('should call initializeForm and setupFormSubscription on ngOnInit', () => {
    spyOn(component, 'initializeForm');
    spyOn(component, 'setupFormSubscription');
    
    component.ngOnInit();
    
    expect(component.initializeForm).toHaveBeenCalled();
    expect(component.setupFormSubscription).toHaveBeenCalled();
  });

  describe('initializeForm', () => {
    it('should create form with correct controls', () => {
      component.initializeForm();
      
      expect(component.breedSearchForm).toBeDefined();
      expect(component.breedSearchForm.get('name')).toBeTruthy();
      expect(component.breedSearchForm.get('characteristics')).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
      component.initializeForm();
      
      expect(component.breedSearchForm.get('name')?.value).toBe('');
      expect(component.breedSearchForm.get('characteristics')?.value).toEqual([]);
    });
  });

  describe('applyFilters', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should emit all breeds when no filters applied', () => {
      spyOn(component.breedsFiltered, 'emit');
      spyOn(console, 'log');
      
      component.applyFilters();
      
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith(mockBreeds);
    });

    it('should filter by name (startsWith)', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ name: 'rag' });
      component.applyFilters();
      
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([mockBreeds[2]]); // Ragdoll
    });

    it('should filter by name case insensitive', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ name: 'PER' });
      component.applyFilters();
      
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([mockBreeds[0]]); // Persian
    });

    it('should filter by characteristics (childFriendly)', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ characteristics: ['childFriendly'] });
      component.applyFilters();
      
      // Persian (5), Siamese (4) and Ragdoll (5) have childFriendly >= 3
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith(mockBreeds);
    });

    it('should filter by lowHealthIssues characteristic', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ characteristics: ['lowHealthIssues'] });
      component.applyFilters();
      
      // Siamese (2) and Ragdoll (3) have healthIssues <= 3, Persian (4)
      const expectedBreeds = [mockBreeds[1], mockBreeds[2]]; // Siamese and Ragdoll
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith(expectedBreeds);
    });

    it('should filter by multiple characteristics', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ 
        characteristics: ['childFriendly', 'affectionLevel'] 
      });
      component.applyFilters();
      
      // Persian (5, 5), Siamese (4, 4), and Ragdoll (5, 5)
      // All three qualify since they all have both >= 3
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith(mockBreeds);
    });

    it('should filter by high standards (5 rating)', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      // Create a test that actually filters out some breeds
      component.breedSearchForm.patchValue({ 
        characteristics: ['energyLevel', 'intelligence'] 
      });
      component.applyFilters();
      
      // Siamese (5, 5) has both energyLevel and intelligence >= 3
      // Persian: energyLevel: 1, intelligence: 3 - fails energyLevel
      // Ragdoll: energyLevel: 2, intelligence: 3 - fails energyLevel
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([mockBreeds[1]]); // Only Siamese
    });

    it('should combine name and characteristics filters', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ 
        name: 'per',
        characteristics: ['childFriendly'] 
      });
      component.applyFilters();
      
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([mockBreeds[0]]); // Only Persian
    });

    it('should return empty array when no matches', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ name: 'nonexistent' });
      component.applyFilters();
      
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([]);
    });
  });

  describe('clearFilters', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should reset form and emit all breeds', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      // Set some values first
      component.breedSearchForm.patchValue({
        name: 'Persian',
        characteristics: ['childFriendly']
      });
      
      component.clearFilters();
      
      expect(component.breedSearchForm.get('name')?.value).toBe('');
      expect(component.breedSearchForm.get('characteristics')?.value).toEqual([]);
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith(mockBreeds);
    });
  });

  describe('getSelectedCharacteristics', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should return empty array when no characteristics selected', () => {
      const result = component.getSelectedCharacteristics();
      expect(result).toEqual([]);
    });

    it('should return characteristic labels', () => {
      component.breedSearchForm.patchValue({
        characteristics: ['childFriendly', 'dogFriendly']
      });
      
      const result = component.getSelectedCharacteristics();
      expect(result).toEqual(['Child Friendly', 'Dog Friendly']);
    });
  });

  describe('removeCharacteristic', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should remove characteristic from form', () => {
      component.breedSearchForm.patchValue({
        characteristics: ['childFriendly', 'dogFriendly', 'energyLevel']
      });
      
      component.removeCharacteristic('Dog Friendly');
      
      const remaining = component.breedSearchForm.get('characteristics')?.value;
      expect(remaining).toEqual(['childFriendly', 'energyLevel']);
    });

    it('should handle removing non-existent characteristic', () => {
      component.breedSearchForm.patchValue({
        characteristics: ['childFriendly']
      });
      
      component.removeCharacteristic('Non Existent');
      
      const remaining = component.breedSearchForm.get('characteristics')?.value;
      expect(remaining).toEqual(['childFriendly']);
    });
  });

  describe('characteristic filtering logic', () => {
    beforeEach(() => {
      component.initializeForm();
    });

    it('should correctly identify high energy breeds', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ characteristics: ['energyLevel'] });
      component.applyFilters();
      
      // Only Siamese (5) has energyLevel >= 3
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([mockBreeds[1]]);
    });

    it('should correctly identify high grooming needs', () => {
      spyOn(component.breedsFiltered, 'emit');
      
      component.breedSearchForm.patchValue({ characteristics: ['grooming'] });
      component.applyFilters();
      
      // Persian (5) and Ragdoll (4) have grooming >= 3
      expect(component.breedsFiltered.emit).toHaveBeenCalledWith([mockBreeds[0], mockBreeds[2]]);
    });
  });
});