import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CatService } from '../../services/cat-service';
import { AuthService } from '../../services/auth-service';
import { Cat } from '../../models/cat-model';

@Component({
  selector: 'app-adoption-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './adoption-form.html',
  styleUrl: './adoption-form.scss'
})
export class AdoptionFormComponent implements OnInit {
  adoptionForm!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdoptionFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { cat: Cat },
    private catService: CatService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.initializeForm();
  }

  initializeForm() {
    this.adoptionForm = this.fb.group({
      experienceWithCats: ['', Validators.required],
      reasonForAdoption: ['', Validators.required]
    });
  }

  onSubmit(): void {
    console.log('Current token:', this.authService.getToken());
    if (this.adoptionForm.valid) {
      this.loading = true;
      
      const currentUser = this.authService.getCurrentUser();
      if (!currentUser) {
        this.snackBar.open('You must be logged in to submit an application', 'Close', {
          duration: 5000,
          panelClass: ['error-snackbar']
        });
        this.loading = false;
        return;
      }

      const notes = `Experience with Cats: ${this.adoptionForm.value.experienceWithCats} \nReason for Adoption: ${this.adoptionForm.value.reasonForAdoption}`;

      const applicationData = {
        userId: currentUser.id,
        catId: this.data.cat.id,
        notes: notes
      };

      this.catService.submitApplication(applicationData).subscribe({
        next: (response) => {
          this.loading = false;
          this.snackBar.open('Adoption application submitted successfully!', 'Close', {
            duration: 5000,
            panelClass: ['success-snackbar']
          });
          this.dialogRef.close({ success: true, adoption: response });
        },
        error: (error) => {
          this.loading = false;
          console.error('Full error object:', error);
          
          let errorMessage = 'Error submitting application. Please try again.';
          
          if (error.status === 400) {
            if (typeof error.error === 'string' && error.error.includes('not available for adoption')) {
              errorMessage = 'This cat is no longer available for adoption. Someone else may have already applied.';
            } else if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error?.message) {
              errorMessage = error.error.message;
            }
          }
          
          this.snackBar.open(errorMessage, 'Close', {
            duration: 7000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }
}