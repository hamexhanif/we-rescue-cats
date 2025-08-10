import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CatBreed } from '../../models/cat-model';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-breed-search',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule
  ],
  templateUrl: './breed-search.html',
  styleUrl: './breed-search.scss'
})
export class BreedSearchComponent implements OnInit {
  @Input() allBreeds: CatBreed[] = [];
  @Output() breedsFiltered = new EventEmitter<CatBreed[]>();
  
  breedSearchForm!: FormGroup;

  characteristicOptions = [
    { value: 'childFriendly', label: 'Child Friendly' },
    { value: 'dogFriendly', label: 'Dog Friendly' },
    { value: 'energyLevel', label: 'High Energy' },
    { value: 'grooming', label: 'High Grooming Needs' },
    { value: 'intelligence', label: 'High Intelligence' },
    { value: 'socialNeeds', label: 'High Social Needs' },
    { value: 'strangerFriendly', label: 'Stranger Friendly' },
    { value: 'adaptability', label: 'Highly Adaptable' },
    { value: 'affectionLevel', label: 'Very Affectionate' },
    { value: 'lowHealthIssues', label: 'Low Health Issues' }
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.initializeForm();
    this.setupFormSubscription();
  }

  initializeForm() {
    this.breedSearchForm = this.fb.group({
      name: [''],
      characteristics: [[]]
    });
  }

  setupFormSubscription() {
    this.breedSearchForm.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  applyFilters() {
    const formValue = this.breedSearchForm.value;
    let filteredBreeds = [...this.allBreeds];

    if (formValue.name && formValue.name.trim()) {
      const searchTerm = formValue.name.toLowerCase().trim();
      filteredBreeds = filteredBreeds.filter(breed => 
        breed.name.toLowerCase().startsWith(searchTerm)
      );
    }

    if (formValue.characteristics && formValue.characteristics.length > 0) {
      filteredBreeds = filteredBreeds.filter(breed => {
        return formValue.characteristics.every((characteristic: string) => {
          if (characteristic === 'lowHealthIssues') {
            // For health issues, use low value (<=3 for good health)
            return breed.healthIssues <= 3;
          } else {
            switch (characteristic) {
              case 'childFriendly':
                return breed.childFriendly >= 3;
              case 'dogFriendly':
                return breed.dogFriendly >= 3;
              case 'energyLevel':
                return breed.energyLevel >= 3;
              case 'grooming':
                return breed.grooming >= 3;
              case 'intelligence':
                return breed.intelligence >= 3;
              case 'socialNeeds':
                return breed.socialNeeds >= 3;
              case 'strangerFriendly':
                return breed.strangerFriendly >= 3;
              case 'adaptability':
                return breed.adaptability >= 3;
              case 'affectionLevel':
                return breed.affectionLevel >= 3;
              default:
                return true;
            }
          }
        });
      });
    }

    console.log(`Filtered breeds: ${filteredBreeds.length} out of ${this.allBreeds.length}`);
    console.log('Applied filters:', {
      name: formValue.name,
      characteristics: formValue.characteristics
    });
    
    this.breedsFiltered.emit(filteredBreeds);
  }

  clearFilters() {
    this.breedSearchForm.reset();
    this.breedSearchForm.patchValue({
      name: '',
      characteristics: []
    });
    this.breedsFiltered.emit(this.allBreeds);
  }

  getSelectedCharacteristics(): string[] {
    const selected = this.breedSearchForm.get('characteristics')?.value || [];
    return selected.map((char: string) => {
      const option = this.characteristicOptions.find(opt => opt.value === char);
      return option ? option.label : char;
    });
  }

  removeCharacteristic(characteristicLabel: string) {
    const currentSelected = this.breedSearchForm.get('characteristics')?.value || [];
    const option = this.characteristicOptions.find(opt => opt.label === characteristicLabel);
    
    if (option) {
      const newSelected = currentSelected.filter((char: string) => char !== option.value);
      this.breedSearchForm.patchValue({ characteristics: newSelected });
    }
  }
}