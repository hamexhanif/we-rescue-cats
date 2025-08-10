import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { AdminService } from '../../../services/admin-service';
import { AdoptionApplication } from '../../../models/cat-model';

@Component({
  selector: 'app-admin-applications',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule
  ],
  templateUrl: './admin-applications.html',
  styleUrl: './admin-applications.scss'
})
export class AdminApplicationsComponent implements OnInit {
  applications: AdoptionApplication[] = [];
  displayedColumns = ['applicant', 'cat', 'submittedAt', 'status', 'actions'];
  pendingCount = 0;
  approvedCount = 0;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadApplications();
  }

  loadApplications() {
    this.adminService.getAllApplications().subscribe({
      next: (applications) => {
        this.applications = applications;
        this.pendingCount = this.applications.filter(app => app.status === 'PENDING').length;
        this.approvedCount = this.applications.filter(app => app.status === 'APPROVED').length;
      },
      error: (error) => {
        console.error('Error loading applications:', error);
        this.snackBar.open('Error loading applications', 'Close', { duration: 3000 });
      }
    });
  }

  approveApplication(application: AdoptionApplication) {
    this.adminService.approveApplication(application.id!).subscribe({
      next: (updatedApplication) => {
        this.snackBar.open('Application approved successfully', 'Close', { duration: 3000 });
        this.loadApplications();
      },
      error: (error) => {
        console.error('Error approving application:', error);
        this.snackBar.open('Error approving application', 'Close', { duration: 3000 });
      }
    });
  }

  completeApplication(application: AdoptionApplication) {
    this.adminService.completeApplication(application.id!).subscribe({
      next: (updatedApplication) => {
        this.snackBar.open('Adoption completed successfully', 'Close', { duration: 3000 });
        this.loadApplications();
      },
      error: (error) => {
        console.error('Error completing application:', error);
        this.snackBar.open('Error completing application', 'Close', { duration: 3000 });
      }
    });
  }

  rejectApplication(application: AdoptionApplication) {
    const reason = prompt('Please enter rejection reason:');
    if (!reason) return;

    this.adminService.rejectApplication(application.id!, reason).subscribe({
      next: (updatedApplication) => {
        this.snackBar.open('Application rejected', 'Close', { duration: 3000 });
        this.loadApplications();
      },
      error: (error) => {
        console.error('Error rejecting application:', error);
        this.snackBar.open('Error rejecting application', 'Close', { duration: 3000 });
      }
    });
  }
}