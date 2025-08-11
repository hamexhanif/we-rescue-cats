import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Cat, CatBreed, AdoptionApplication, CreateCatRequest, ApiResponse } from '../models/cat-model';

@Injectable({
  providedIn: 'root'
})
export class CatService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getAllCats(): Observable<Cat[]> {
    return this.http.get<any>(`${this.apiUrl}/cats`).pipe(
      map(response => {
        const cats = response.content || response.data || response;
        
        return cats.map((cat: any) => ({
          id: cat.id,
          name: cat.name,
          age: cat.age,
          gender: cat.gender,
          description: cat.description,
          breedId: cat.breedId,
          breedName: cat.breedName,
          imageUrl: cat.imageUrl,
          latitude: cat.latitude,
          longitude: cat.longitude,
          address: cat.address,
          adoptionStatus: cat.status,
          createdAt: cat.createdAt,
          updatedAt: cat.updatedAt
        }));
      }),
    );
  }

  getCatById(id: number): Observable<Cat> {
    return this.http.get<any>(`${this.apiUrl}/cats/${id}`).pipe(
      map(response => {
        const cat = response.content || response.data || response;
        
        return {
          id: cat.id,
          name: cat.name,
          age: cat.age,
          gender: cat.gender,
          description: cat.description,
          breedId: cat.breedId,
          breedName: cat.breedName,
          imageUrl: cat.imageUrl,
          latitude: cat.latitude,
          longitude: cat.longitude,
          address: cat.address,
          adoptionStatus: cat.status,
          createdAt: cat.createdAt,
          updatedAt: cat.updatedAt
        };
      }),
    );
  }

  getAvailableCats(): Observable<Cat[]> {
    return this.http.get<any>(`${this.apiUrl}/cats/available`).pipe(
      map(response => {
        const cats = response.content || response.data || response;
        
        return cats.map((cat: any) => ({
          id: cat.id,
          name: cat.name,
          age: cat.age,
          gender: cat.gender,
          description: cat.description,
          breedId: cat.breedId,
          breedName: cat.breedName,
          imageUrl: cat.imageUrl,
          latitude: cat.latitude,
          longitude: cat.longitude,
          address: cat.address,
          adoptionStatus: cat.status,
          createdAt: cat.createdAt,
          updatedAt: cat.updatedAt
        }));
      }),
    );
  }

  submitApplication(applicationData: { userId: number; catId: number; notes: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/adoptions`, applicationData);
  }

  getUserAdoptions(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/adoptions/user/${userId}`);
  }

  getAllApplications(): Observable<ApiResponse<AdoptionApplication[]>> {
    return this.http.get<ApiResponse<AdoptionApplication[]>>(`${this.apiUrl}/admin/adoptions`);
  }

  getCatBreeds(): Observable<CatBreed[]> {
    return this.http.get<any>(`${this.apiUrl}/breeds`).pipe(
      map(response => {
        const breeds = response.content || response;
        return breeds.map((breed: { childFriendly: any; child_friendly: any; energyLevel: any; energy_level: any; dogFriendly: any; dog_friendly: any; grooming: any; healthIssues: any; health_issues: any; intelligence: any; socialNeeds: any; social_needs: any; strangerFriendly: any; stranger_friendly: any; adaptability: any; affectionLevel: any; affection_level: any; wikipediaUrl: any; wikipedia_url: any; referenceImageId: any; reference_image_id: any; imageUrl: any; image_url: any; }) => ({
          ...breed,
          childFriendly: breed.childFriendly || breed.child_friendly,
            energyLevel: breed.energyLevel || breed.energy_level,
            dogFriendly: breed.dogFriendly || breed.dog_friendly,
            grooming: breed.grooming,
            healthIssues: breed.healthIssues || breed.health_issues,
            intelligence: breed.intelligence,
            socialNeeds: breed.socialNeeds || breed.social_needs,
            strangerFriendly: breed.strangerFriendly || breed.stranger_friendly,
            adaptability: breed.adaptability,
            affectionLevel: breed.affectionLevel || breed.affection_level,
          wikipediaUrl: breed.wikipediaUrl || breed.wikipedia_url,
          referenceImageId: breed.referenceImageId || breed.reference_image_id,
          imageUrl: breed.imageUrl || breed.image_url
        }));
      })
    );
  }

  getStats(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.apiUrl}/stats`);
  }
}
