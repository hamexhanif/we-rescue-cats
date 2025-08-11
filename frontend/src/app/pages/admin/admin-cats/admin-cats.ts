import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CatService } from '../../../services/cat-service';
import { Cat } from '../../../models/cat-model';
import { AdminService } from '../../../services/admin-service';
import { CreateCatDialogComponent } from '../../../components/create-cat-form/create-cat-form';

@Component({
  selector: 'app-admin-cats',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule
  ],
  templateUrl: './admin-cats.html' ,
  styleUrl: './admin-cats.scss'
})
export class AdminCatsComponent implements OnInit {
  cats: Cat[] = [];
  displayedColumns = ['image', 'name', 'details', 'status', 'location', 'actions'];

  constructor(
    private catService: CatService,
    private adminService: AdminService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit() {
    this.loadCats();
  }

  loadCats() {
    this.catService.getAllCats().subscribe({
      next: (cats) => {
        if (cats) {
          this.cats = cats;
        }
      },
      error: (error) => {
        console.error('Error loading cats:', error);
        this.snackBar.open('Error loading cats', 'Close', { duration: 3000 });
      }
    });
  }

  addNewCat() {
    const dialogRef = this.dialog.open(CreateCatDialogComponent, {
      width: '600px',
      maxHeight: '90vh',
      disableClose: false,
      autoFocus: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loadCats();
      }
    });
  }

  deleteCat(cat: Cat) {
    if (confirm(`Are you sure you want to delete ${cat.name}?`)) {
      this.adminService.deleteCat(cat.id).subscribe({
        next: (response) => {
          if (response.success) {
            this.snackBar.open('Cat deleted successfully', 'Close', { duration: 3000 });
            this.loadCats();
          }
        },
        error: (error) => {
          console.error('Error deleting cat:', error);
          this.snackBar.open('Error deleting cat', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
