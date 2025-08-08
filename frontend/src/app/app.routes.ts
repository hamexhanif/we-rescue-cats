import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { BreedsComponent } from './pages/breeds/breeds';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'breeds',
    component: BreedsComponent
  }
];
