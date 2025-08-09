import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CatService } from '../../services/cat-service';
import { Cat } from '../../models/cat-model';
import { CatCardComponent } from '../../components/cat-card/cat-card';
import { SearchComponent } from '../../components/search/search';

@Component({
  selector: 'app-cats',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTabsModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    CatCardComponent,
    SearchComponent
  ],
  templateUrl: './cats.html',
  styleUrl: './cats.scss'
})
export class CatsComponent implements OnInit {
  cats: Cat[] = [];
  filteredCats: Cat[] = [];
  loading = true;

  pageSize = 12;
  pageIndex = 0;
  totalCats = 0;

  currentFilters: any = {};
  selectedTabIndex = 0;

  constructor(
    private catService: CatService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.currentFilters = { ...params };
      this.loadAvailableCats();
    });
  }

  loadAvailableCats() {
    this.loading = true;
    this.catService.getAvailableCats().subscribe({
      next: (cats) => {
        if (cats) {
          this.cats = cats;
          this.applyTabFilter();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading cats:', error);
        this.loading = false;
        this.cats = this.getMockCats();
        this.applyTabFilter();
      }
    });
  }

  onFiltersChanged(filters: any) {
    this.currentFilters = filters;
    this.pageIndex = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: filters,
      queryParamsHandling: 'merge'
    });
    this.loadAvailableCats();
  }

  onTabChange(tabIndex: number) {
    this.selectedTabIndex = tabIndex;
    this.pageIndex = 0;
    this.applyTabFilter();
  }

  applyTabFilter() {
    let filtered = [...this.cats];

    switch (this.selectedTabIndex) {
      case 1: // Available
        filtered = filtered.filter(cat => cat.adoptionStatus === 'AVAILABLE');
        break;
      case 2: // Pending
        filtered = filtered.filter(cat => cat.adoptionStatus === 'PENDING');
        break;
      default: // All
        break;
    }

    this.filteredCats = filtered;
    this.totalCats = filtered.length;
  }


  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  get pagedCats(): Cat[] {
    const startIndex = this.pageIndex * this.pageSize;
    return this.filteredCats.slice(startIndex, startIndex + this.pageSize);
  }

  clearFilters() {
    this.currentFilters = {};
    this.selectedTabIndex = 0;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {},
    });
    this.loadAvailableCats();
  }

  private getMockCats(): Cat[] {
    return [
      {
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
        imageUrl: 'https://cdn2.thecatapi.com/images/O3btzLlsO.png',
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z'
      },
      {
        id: 2,
        name: 'Max',
        age: 2,
        gender: 'MALE',
        breedId: 'pers',
        breedName: 'Persian Mix',
        description: 'Sweet and gentle cat looking for a loving home',
        adoptionStatus: 'AVAILABLE',
        latitude: 53.124,
        longitude: 14.223,
        address: 'Dresden',
        imageUrl: 'https://cdn2.thecatapi.com/images/O3btzLlsO.png',
        createdAt: '2024-01-15T10:00:00Z',
        updatedAt: '2024-01-15T10:00:00Z'
      }
    ];
  }
}
