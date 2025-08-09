import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { BreedsComponent } from './pages/breeds/breeds';
import { BreedDetailComponent } from './pages/breed-detail/breed-detail';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'breeds',
    component: BreedsComponent
  },
  {
    path: 'breeds/:id',
    component: BreedDetailComponent
  }
];
