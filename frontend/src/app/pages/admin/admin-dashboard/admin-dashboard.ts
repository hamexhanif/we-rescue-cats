import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatGridListModule } from '@angular/material/grid-list';
import { AdminService } from '../../../services/admin-service';
import { DashboardStats } from '../../../models/dashboard-stats-model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatGridListModule
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboardComponent implements OnInit {
  stats: any = {};
  lstats: DashboardStats = {} as DashboardStats;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.adminService.getDashboardStats().subscribe({
        next: (stats: DashboardStats) => {
            this.stats = stats;
        },
      error: (error) => {
        console.error('Error loading stats:', error);
        this.stats = {
          totalCats: 156,
          availableCats: 120,
          adoptedCats: 89,
          totalUsers: 1247,
          adminUsers: 5,
          totalAdoptions: 234,
          pendingAdoptions: 23,
          completedAdoptions: 211
        };
      }
    });
  }
}
