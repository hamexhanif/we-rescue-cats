import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CatCardComponent } from './cat-card';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { Cat } from '../../models/cat-model';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('CatCardComponent', () => {
  let component: CatCardComponent;
  let fixture: ComponentFixture<CatCardComponent>;
  let router: any;
  let dialog: any;

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
    imageUrl: 'https://example.com/luna.jpg',
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-15T10:00:00Z'
  };

  const mockAdoptedCat: Cat = {
    ...mockCat,
    id: 2,
    name: 'Max',
    adoptionStatus: 'ADOPTED'
  };

  beforeEach(async () => {
    const routerSpy = {
      navigate: jasmine.createSpy('navigate')
    };
    
    const dialogSpy = {
      open: jasmine.createSpy('open')
    };

    await TestBed.configureTestingModule({
      imports: [
        CatCardComponent,
        NoopAnimationsModule,
        HttpClientTestingModule
      ],
      providers: [
        provideRouter([]),
        { provide: Router, useValue: routerSpy },
        { provide: MatDialog, useValue: dialogSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CatCardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    dialog = TestBed.inject(MatDialog);

    component.cat = mockCat;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should require cat input', () => {
    expect(component.cat).toBeDefined();
    expect(component.cat).toEqual(mockCat);
  });

  describe('viewCatDetails', () => {
    it('should navigate to cat details page', () => {
      component.viewCatDetails();
      
      expect(router.navigate).toHaveBeenCalledWith(['/cats', mockCat.id]);
    });

    it('should navigate with correct cat id', () => {
      const catWithDifferentId = { ...mockCat, id: 99 };
      component.cat = catWithDifferentId;
      
      component.viewCatDetails();
      
      expect(router.navigate).toHaveBeenCalledWith(['/cats', 99]);
    });
  });

  describe('openAdoptionForm', () => {
    it('should not open dialog for adopted cat', () => {
      component.cat = mockAdoptedCat;
      
      component.openAdoptionForm();
      
      expect(dialog.open).not.toHaveBeenCalled();
    });

    it('should not open dialog for pending cat', () => {
      const pendingCat: Cat = { ...mockCat, adoptionStatus: 'PENDING' };
      component.cat = pendingCat;
      
      component.openAdoptionForm();
      
      expect(dialog.open).not.toHaveBeenCalled();
    });
  });

  describe('cat input handling', () => {
    it('should handle cat with all properties', () => {
      const fullCat: Cat = {
        id: 5,
        name: 'Whiskers',
        age: 3,
        gender: 'MALE',
        breedId: 'siam',
        breedName: 'Siamese',
        description: 'Very friendly cat',
        adoptionStatus: 'AVAILABLE',
        latitude: 51.0504,
        longitude: 13.7373,
        address: 'Leipzig',
        imageUrl: 'https://example.com/whiskers.jpg',
        createdAt: '2024-02-01T10:00:00Z',
        updatedAt: '2024-02-01T10:00:00Z'
      };

      component.cat = fullCat;
      
      expect(component.cat).toEqual(fullCat);
      expect(component.cat.name).toBe('Whiskers');
      expect(component.cat.adoptionStatus).toBe('AVAILABLE');
    });

    it('should handle cat without image', () => {
      const catWithoutImage = { ...mockCat, imageUrl: undefined };
      component.cat = catWithoutImage;
      
      expect(component.cat.imageUrl).toBeUndefined();
    });
  });

  describe('adoption status checks', () => {
    it('should block adoption for non-available statuses', () => {
      const statuses: ('ADOPTED' | 'PENDING')[] = ['ADOPTED', 'PENDING'];
      
      statuses.forEach(status => {
        dialog.open.calls.reset();
        const testCat: Cat = { ...mockCat, adoptionStatus: status };
        component.cat = testCat;
        
        component.openAdoptionForm();
        
        expect(dialog.open).not.toHaveBeenCalled();
      });
    });
  });

  describe('openAdoptionForm method exists', () => {
    it('should have openAdoptionForm method', () => {
      expect(typeof component.openAdoptionForm).toBe('function');
    });
  });
});