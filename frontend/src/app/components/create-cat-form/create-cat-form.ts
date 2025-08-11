import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, startWith, map } from 'rxjs';
import { AdminService } from '../../services/admin-service';
import { CatService } from '../../services/cat-service';
import { CreateCatRequest, CatBreed } from '../../models/cat-model';

@Component({
    selector: 'app-create-cat-dialog',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatButtonModule,
        MatIconModule,
        MatAutocompleteModule
    ],
    templateUrl: './create-cat-form.html',
    styleUrl: './create-cat-form.scss'
})
export class CreateCatDialogComponent implements OnInit {
    catForm: FormGroup;
    isSubmitting = false;
    
    breeds: CatBreed[] = [];
    filteredBreeds: Observable<CatBreed[]>;

    constructor(
        private fb: FormBuilder,
        private dialogRef: MatDialogRef<CreateCatDialogComponent>,
        private adminService: AdminService,
        private catService: CatService,
        private snackBar: MatSnackBar,
        @Inject(MAT_DIALOG_DATA) public data: any
    ) {
        this.catForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(1)]],
            age: ['', [Validators.required, Validators.min(0), Validators.max(25)]],
            gender: ['', Validators.required],
            description: ['', Validators.required],
            breed: ['', Validators.required],
            breedId: [''],
            breedName: [''],
            imageUrl: [''],
            latitude: ['', Validators.required],
            longitude: ['', Validators.required],
            address: ['', Validators.required]
        });

        this.filteredBreeds = this.catForm.get('breed')!.valueChanges.pipe(
            startWith(''),
            map(value => this._filter(value || ''))
        );
    }

    ngOnInit() {
        this.loadBreeds();
    }

    loadBreeds() {
        this.catService.getCatBreeds().subscribe({
            next: (breeds) => {
                this.breeds = breeds;
                console.log('Loaded breeds:', breeds);
            },
            error: (error) => {
                console.error('Error loading breeds:', error);
                this.snackBar.open('Error loading breeds', 'Close', { duration: 3000 });
            }
        });
    }

    private _filter(value: string): CatBreed[] {
        if (typeof value !== 'string') {
            return this.breeds;
        }
        
        const filterValue = value.toLowerCase();
        return this.breeds.filter(breed => 
            breed.name.toLowerCase().startsWith(filterValue)
        );
    }

    onBreedSelected(event: any) {
        const breed = event.option.value;
        this.catForm.patchValue({
            breed: breed.name,
            breedId: breed.id,
            breedName: breed.name
        });
    }

    displayBreed(breed: CatBreed | string): string {
        if (typeof breed === 'string') {
            return breed;
        }
        return breed ? breed.name : '';
    }

    onSubmit() {
        if (this.catForm.valid) {
            this.isSubmitting = true;

            const formValue = this.catForm.value;

            const catData: CreateCatRequest = {
                name: formValue.name,
                age: parseInt(formValue.age),
                gender: formValue.gender,
                description: formValue.description,
                breedId: formValue.breedId,
                breedName: formValue.breedName,
                imageUrl: formValue.imageUrl || null,
                latitude: parseFloat(formValue.latitude),
                longitude: parseFloat(formValue.longitude),
                address: formValue.address,
                status: 'AVAILABLE'
            };

            this.adminService.createCat(catData).subscribe({
                next: (response) => {
                    this.isSubmitting = false;
                    console.log('Cat creation successful:', response);
                    if (response) {
                        this.snackBar.open('Cat created successfully!', 'Close', { 
                            duration: 3000,
                            panelClass: ['success-snackbar']
                        });
                        this.dialogRef.close(true);
                    }
                    // else {
                    //     this.snackBar.open('Failed to create cat. Please try again.', 'Close', { 
                    //         duration: 5000,
                    //         panelClass: ['error-snackbar']
                    //     });
                    // }
                },
                error: (error) => {
                    this.isSubmitting = false;
                    let errorMessage = 'Error creating cat. Please try again.';
                    if (error.status === 403) {
                        errorMessage = 'Access denied. Please check your permissions.';
                    } else if (error.status === 401) {
                        errorMessage = 'Authentication required. Please log in again.';
                    } else if (error.status === 400) {
                        errorMessage = 'Invalid data. Please check your input.';
                    }

                    this.snackBar.open(errorMessage, 'Close', { 
                        duration: 5000,
                        panelClass: ['error-snackbar']
                    });
                }
            });
        } else {
            console.log('Form is invalid:', this.catForm.errors);
            this.markFormGroupTouched();
        }
    }

    onCancel() {
        this.dialogRef.close(false);
    }

    private markFormGroupTouched() {
        Object.keys(this.catForm.controls).forEach(key => {
            const control = this.catForm.get(key);
            control?.markAsTouched();
        });
    }
}