import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { BreedsComponent } from './pages/breeds/breeds';
import { BreedDetailComponent } from './pages/breed-detail/breed-detail';
import { CatsComponent } from './pages/cats/cats';
import { CatDetailComponent } from './pages/cat-detail/cat-detail';
import { LoginComponent } from './pages/auth/login/login';
import { RegisterComponent } from './pages/auth/register/register';
import { ProfileComponent } from './pages/profile/profile';

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
  },
  {
    path: 'cats',
    component: CatsComponent
  },
  {
    path: 'cats/:id',
    component: CatDetailComponent
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'profile',
    component: ProfileComponent
  },
];
