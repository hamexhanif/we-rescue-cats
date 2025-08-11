import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MapComponent } from './map';
import { CatService } from '../../services/cat-service';
import { Cat } from '../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;
  let catService: any;

  const mockCats: Cat[] = [
    {
      id: 1,
      name: 'Luna',
      age: 2,
      gender: 'FEMALE',
      breedId: 'pers',
      breedName: 'Persian Mix',
      description: 'Sweet cat',
      adoptionStatus: 'AVAILABLE',
      latitude: 51.0504,
      longitude: 13.7373,
      address: 'Dresden',
      imageUrl: 'luna.jpg',
      createdAt: '2024-01-15T10:00:00Z',
      updatedAt: '2024-01-15T10:00:00Z'
    }
  ];

  beforeEach(async () => {
    const catServiceSpy = {
      getAvailableCats: jasmine.createSpy('getAvailableCats').and.returnValue(of(mockCats))
    };

    await TestBed.configureTestingModule({
      imports: [
        MapComponent,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: CatService, useValue: catServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    catService = TestBed.inject(CatService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with loading = true', () => {
    expect(component.loading).toBe(true);
  });

  it('should call loadCats on ngOnInit', () => {
    spyOn(component as any, 'loadCats');
    
    component.ngOnInit();
    
    expect((component as any).loadCats).toHaveBeenCalled();
  });

  it('should load cats successfully', () => {
    (component as any).loadCats();
    
    expect(catService.getAvailableCats).toHaveBeenCalled();
    expect(component.loading).toBe(false);
  });

  it('should handle error when loading cats', () => {
    spyOn(console, 'error');
    catService.getAvailableCats.and.returnValue(throwError(() => new Error('Service error')));
    
    (component as any).loadCats();
    
    expect(console.error).toHaveBeenCalledWith('Error loading cats:', jasmine.any(Error));
    expect(component.loading).toBe(false);
  });

  it('should have lifecycle methods', () => {
    expect(typeof component.ngOnInit).toBe('function');
    expect(typeof component.ngAfterViewInit).toBe('function');
    expect(typeof component.ngOnDestroy).toBe('function');
  });

  it('should not throw errors on ngOnInit and ngAfterViewInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
    expect(() => component.ngAfterViewInit()).not.toThrow();
  });

  it('should handle ngOnDestroy safely when no map exists', () => {
    
    // Ensure map is null/undefined
    (component as any).map = null;
    expect(() => component.ngOnDestroy()).not.toThrow();
  });
});