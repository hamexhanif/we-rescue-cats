import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { AuthService } from '../../services/auth-service';
import { CatService } from '../../services/cat-service';
import { User } from '../../models/auth-model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class ProfileComponent implements OnInit {
  currentUser$: Observable<User | null>;
  applications: any[] = [];
  displayedColumns: string[] = ['catName', 'adoptionDate', 'status'];

  constructor(
    private authService: AuthService,
    private catService: CatService
  ) {
    this.currentUser$ = this.authService.currentUser$;
  }

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications() {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.catService.getUserAdoptions(currentUser.id).subscribe({
        next: (adoptions) => {
          this.applications = adoptions;
        },
        error: (error) => {
          console.error('Error loading applications:', error);
        }
      });
    }
  }
}