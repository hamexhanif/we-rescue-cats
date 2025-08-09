import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { CatService } from '../../services/cat-service';
import { CatBreed, Cat } from '../../models/cat-model';

@Component({
  selector: 'app-breed-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatDividerModule
  ],
  templateUrl: './breed-detail.html',
  styleUrl: './breed-detail.scss'
})
export class BreedDetailComponent implements OnInit {
  breed: CatBreed | null = null;
  availableCats: Cat[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private catService: CatService
  ) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      const breedId = params['id'];
      this.loadBreed(breedId);
      // this.loadAvailableCats();
    });
  }

  loadBreed(id: string) {
    this.loading = true;
    this.catService.getTheCatBreeds().subscribe({
      next: (breeds) => {
        this.breed = breeds.find(breed => breed.id === id) || null;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading breed:', error);
        this.loading = false;
      }
    });
  }

  // loadAvailableCats(breedId: string) {
  //   // Filter cats by breed
  //   this.catService.getAllCats({ breed: breedId }).subscribe({
  //     next: (response) => {
  //       if (response.success) {
  //         this.availableCats = response.data.slice(0, 6);
  //       }
  //     },
  //     error: (error) => {
  //       console.error('Error loading cats:', error);
  //     }
  //   });
  // }

  getChildFriendlyDescription(rating: number): string {
    const descriptions = [
      'Not recommended for homes with children',
      'Better with older, gentle children',
      'Good with well-behaved children',
      'Great with children of all ages',
      'Excellent family cat, very patient with children'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getDogFriendlyDescription(rating: number): string {
    const descriptions = [
      'Typically does not get along well with dogs',
      'May tolerate dogs with proper introduction',
      'Generally gets along with dogs',
      'Usually enjoys the company of dogs',
      'Excellent with dogs, often forms close bonds'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getEnergyLevelDescription(rating: number): string {
    const descriptions = [
      'Very calm and low-energy',
      'Prefers quiet activities and rest',
      'Moderate energy, enjoys play sessions',
      'Active and playful throughout the day',
      'Very high energy, needs lots of stimulation'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getGroomingDescription(rating: number): string {
    const descriptions = [
      'Very low maintenance grooming',
      'Occasional brushing needed',
      'Regular weekly brushing recommended',
      'Needs frequent grooming and care',
      'Daily grooming essential to prevent matting'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getHealthDescription(rating: number): string {
    const descriptions = [
      'Generally very healthy breed',
      'Few known health issues',
      'Some breed-specific health concerns',
      'Several potential health issues to monitor',
      'Higher risk of breed-related health problems'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getIntelligenceDescription(rating: number): string {
    const descriptions = [
      'Simple and straightforward personality',
      'Basic learning ability',
      'Average intelligence, trainable',
      'Highly intelligent and quick to learn',
      'Exceptionally intelligent, problem-solving abilities'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getSheddingDescription(rating: number): string {
    const descriptions = [
      'Minimal shedding',
      'Light shedding, easy to manage',
      'Moderate shedding, regular brushing helps',
      'Heavy shedding, frequent grooming needed',
      'Very heavy shedding, daily maintenance required'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getSocialNeedsDescription(rating: number): string {
    const descriptions = [
      'Very independent, enjoys solitude',
      'Somewhat independent but appreciates company',
      'Enjoys both alone time and social interaction',
      'Thrives on social interaction and attention',
      'Extremely social, needs constant companionship'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getStrangerFriendlyDescription(rating: number): string {
    const descriptions = [
      'Very shy and reserved with strangers',
      'Cautious but may warm up to new people',
      'Neutral towards strangers, neither shy nor outgoing',
      'Generally friendly and welcoming to visitors',
      'Extremely outgoing, loves meeting new people'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getAdaptabilityDescription(rating: number): string {
    const descriptions = [
      'Struggles with changes, prefers stable routines',
      'Needs time to adjust to new situations or environments',
      'Moderately adaptable, handles changes with some preparation',
      'Quite adaptable, adjusts well to new homes and situations',
      'Extremely adaptable, thrives in any environment or situation'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getAffectionLevelDescription(rating: number): string {
    const descriptions = [
      'Very independent, shows minimal physical affection',
      'Occasionally affectionate but prefers personal space',
      'Moderately affectionate, enjoys some cuddles and attention',
      'Very loving and seeks out human attention regularly',
      'Extremely affectionate, constant companion who loves cuddles'
    ];
    return descriptions[rating - 1] || descriptions[2];
  }

  getFeedingTips(): string {
    if (!this.breed) return '';

    if (this.breed.energyLevel >= 4) {
      return 'High-energy breeds need protein-rich diets. Feed smaller, frequent meals to maintain energy levels throughout the day.';
    } else if (this.breed.energyLevel <= 2) {
      return 'Lower energy breeds are prone to weight gain. Monitor portions carefully and choose high-quality, balanced nutrition.';
    }
    return 'Provide high-quality cat food appropriate for age and activity level. Fresh water should always be available.';
  }

  getGroomingTips(): string {
    if (!this.breed) return '';

    const groomingLevel = this.breed.grooming;
    if (groomingLevel >= 4) {
      return 'Daily brushing is essential to prevent matting. Consider professional grooming every 6-8 weeks.';
    } else if (groomingLevel >= 3) {
      return 'Brush 2-3 times per week to maintain coat health and reduce shedding around the home.';
    } else if (groomingLevel >= 2) {
      return 'Weekly brushing helps maintain coat condition and provides bonding time with your cat.';
    }
    return 'Minimal grooming needed. Occasional brushing and regular nail trims are sufficient.';
  }

  getExerciseTips(): string {
    if (!this.breed) return '';

    const energyLevel = this.breed.energyLevel;
    if (energyLevel >= 4) {
      return 'Needs multiple active play sessions daily. Provide climbing trees, interactive toys, and puzzle feeders.';
    } else if (energyLevel >= 3) {
      return 'Enjoys regular play sessions. Interactive toys and climbing opportunities keep them mentally stimulated.';
    } else if (energyLevel >= 2) {
      return 'Moderate exercise needs. Short play sessions and comfortable perches for observation are ideal.';
    }
    return 'Low exercise needs. Gentle play and comfortable resting spots suit their calm nature.';
  }

  getHealthTips(): string {
    if (!this.breed) return '';

    const healthIssues = this.breed.healthIssues;
    if (healthIssues >= 4) {
      return 'Regular veterinary check-ups are crucial. Be aware of breed-specific health concerns and maintain preventive care.';
    } else if (healthIssues >= 3) {
      return 'Annual vet visits and monitoring for breed-related health issues. Maintain good dental hygiene.';
    } else if (healthIssues >= 2) {
      return 'Standard veterinary care with annual check-ups. Monitor weight and provide good nutrition.';
    }
    return 'Generally healthy breed. Regular vet visits and basic preventive care are usually sufficient.';
  }
}