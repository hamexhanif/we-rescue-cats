import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CatService } from '../../services/cat-service';
import { CatBreed } from '../../models/cat-model';

@Component({
  selector: 'app-cat-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './cat-search.html',
  styleUrl: './cat-search.scss'
})
export class CatSearchComponent implements OnInit {
  @Output() filtersChanged = new EventEmitter<any>();
  
  catSearchForm!: FormGroup;
  allBreeds: CatBreed[] = [];
  breedNames: string[] = [];

  ageRanges = [
    { value: '', label: 'Any Age' },
    { value: '0-2', label: 'Young (0-2 years)' },
    { value: '3-7', label: 'Adult (3-7 years)' },
    { value: '8+', label: 'Senior (8+ years)' }
  ];

  genderOptions = [
    { value: '', label: 'Any Gender' },
    { value: 'MALE', label: 'Male' },
    { value: 'FEMALE', label: 'Female' }
  ];

  constructor(
    private fb: FormBuilder,
    private catService: CatService
  ) {}

  ngOnInit() {
    this.initializeForm();
    this.loadBreeds();
  }

  initializeForm() {
    this.catSearchForm = this.fb.group({
      breed: [''],
      ageRange: [''],
      gender: [''],
      location: ['']
    });
  }

  loadBreeds() {
    this.catService.getCatBreeds().subscribe({
      next: (breeds: CatBreed[]) => {
        this.allBreeds = breeds;
        this.breedNames = breeds.map(breed => breed.name);
      },
      error: (error) => {
        console.error('Error loading breeds:', error);
        this.breedNames = [
          'Persian', 'Siamese', 'Maine Coon', 'British Shorthair', 
          'Ragdoll', 'Russian Blue', 'Domestic Shorthair', 'Bengal'
        ];
      }
    });
  }

  onSearch() {
    const filters: any = {};
    const formValue = this.catSearchForm.value;

    Object.keys(formValue).forEach(key => {
      if (formValue[key] && formValue[key].trim() !== '') {
        filters[key] = formValue[key];
      }
    });

    console.log('Emitting filters:', filters);
    this.filtersChanged.emit(filters);
  }

  clearFilters() {
    this.catSearchForm.reset();
    this.filtersChanged.emit({});
  }
}