import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { CatService } from '../../services/cat-service';
import { CatBreed } from '../../models/cat-model';
import { BreedSearchComponent } from '../../components/breed-search/breed-search';

@Component({
  selector: 'app-breeds',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatChipsModule,
    BreedSearchComponent
  ],
  templateUrl: './breeds.html',
  styleUrl: './breeds.scss'
})
export class BreedsComponent implements OnInit {
  breeds: CatBreed[] = [];
  loading = true;
  allBreeds: CatBreed[] = [];

  constructor(private catService: CatService) { }

  ngOnInit() {
    this.loadBreeds();
  }

  loadBreeds() {
    this.catService.getCatBreeds().subscribe({
      next: (breeds) => {
        this.allBreeds = breeds;
        this.breeds = breeds;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading breeds:', error);
        this.loading = false;
        this.allBreeds = this.getMockBreeds();
        this.breeds = this.allBreeds;
      }
    });
  }

  onBreedsFiltered(filteredBreeds: CatBreed[]) {
    this.breeds = filteredBreeds;
  }

  private getMockBreeds(): CatBreed[] {
    return [
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
      }
    ];
  }
}
