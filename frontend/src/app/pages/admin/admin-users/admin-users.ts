import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AdminService } from '../../../services/admin-service';
import { User } from '../../../models/auth-model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule
  ],
  templateUrl: './admin-user.html',
  styleUrl: './admin-user.scss'
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  displayedColumns = ['user', 'role', 'createdAt', 'actions'];
  adminCount = 0;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        if (users) {
          this.users = users;
          this.adminCount = this.users.filter(user => user.role === 'ADMIN').length;
        }
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.snackBar.open('Error loading users', 'Close', { duration: 3000 });
      }
    });
  }

  deleteUser(user: User) {  
    if (user.role === 'ADMIN') {
      this.snackBar.open('Cannot delete admin users', 'Close', { duration: 3000 });
      return;
    }

    if (confirm(`Are you sure you want to delete user ${user.email}?`)) {
      this.adminService.deleteUser(user.id).subscribe({
        next: () => {
            this.snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
            this.loadUsers();
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.snackBar.open('Error deleting user', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
