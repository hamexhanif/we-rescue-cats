import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Cat } from '../../models/cat-model';
import { AdoptionFormComponent } from '../adoption-form/adoption-form';

@Component({
  selector: 'app-cat-card',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule
  ],
  templateUrl: './cat-card.html' ,
  styleUrl: './cat-card.scss'
})
export class CatCardComponent {
  @Input() cat!: Cat;

  constructor(
    private router: Router,
    private dialog: MatDialog
  ) {}

  viewCatDetails() {
    this.router.navigate(['/cats', this.cat.id]);
  }

  openAdoptionForm() {
    if (this.cat.adoptionStatus !== 'AVAILABLE') return;

    const dialogRef = this.dialog.open(AdoptionFormComponent, {
      width: '600px',
      data: { cat: this.cat }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Adoption application submitted:', result);
      }
    });
  }
}
