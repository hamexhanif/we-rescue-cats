import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { CatService } from '../../services/cat-service';
import { Cat } from '../../models/cat-model';
import { AdoptionFormComponent } from '../../components/adoption-form/adoption-form';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-cat-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './cat-detail.html',
  styleUrl: './cat-detail.scss'
})
export class CatDetailComponent implements OnInit {
  cat: Cat | null = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catService: CatService,
    private authService: AuthService,
    private dialog: MatDialog
  ) { }

  ngOnInit() {
    this.route.params.subscribe(params => {
      const catId = +params['id'];
      this.loadCat(catId);
    });
  }

  loadCat(id: number) {
    this.loading = true;
    this.catService.getCatById(id).subscribe({
      next: (cat) => {
        if (cat) {
          this.cat = cat;
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading cat:', error);
        this.loading = false;
        this.cat = this.getMockCat(id);
      }
    });
  }

  openAdoptionForm() {
    if(this.authService.isAuthenticated()){
      const dialogRef = this.dialog.open(AdoptionFormComponent, {
        width: '600px',
        data: { cat: this.cat }
      });
  
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          console.log('Adoption application submitted:', result);
        }
      });
    }else{
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
    }    
  }

  private getMockCat(id: number): Cat {
    return {
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
    };
  }
}
