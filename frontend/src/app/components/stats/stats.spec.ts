import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatsComponent } from './stats';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('StatsComponent', () => {
  let component: StatsComponent;
  let fixture: ComponentFixture<StatsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StatsComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(StatsComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should start with empty stats object', () => {
    expect(component.stats).toEqual({});
  });

  it('should accept stats input', () => {
    const testStats = {
      catsRescued: 100,
      happyFamilies: 80,
      rescuePartners: 10,
      support: '24/7'
    };

    component.stats = testStats;
    
    expect(component.stats).toEqual(testStats);
  });

  it('should handle stats with all properties', () => {
    const fullStats = {
      catsRescued: 2847,
      happyFamilies: 1923,
      rescuePartners: 156,
      support: '24/7'
    };

    component.stats = fullStats;
    
    expect(component.stats.catsRescued).toBe(2847);
    expect(component.stats.happyFamilies).toBe(1923);
    expect(component.stats.rescuePartners).toBe(156);
    expect(component.stats.support).toBe('24/7');
  });

  it('should handle stats with partial properties', () => {
    const partialStats = {
      catsRescued: 50
    };

    component.stats = partialStats;
    
    expect(component.stats.catsRescued).toBe(50);
    expect(component.stats.happyFamilies).toBeUndefined();
  });

  it('should handle null stats', () => {
    component.stats = null;
    
    expect(component.stats).toBe(null);
  });

  it('should handle undefined stats', () => {
    component.stats = undefined;
    
    expect(component.stats).toBe(undefined);
  });

  it('should have stats as Input property', () => {
    expect(component.hasOwnProperty('stats')).toBe(true);
  });
});