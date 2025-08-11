import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { BreedsComponent } from './pages/breeds/breeds';
import { BreedDetailComponent } from './pages/breed-detail/breed-detail';
import { CatsComponent } from './pages/cats/cats';
import { CatDetailComponent } from './pages/cat-detail/cat-detail';
import { LoginComponent } from './pages/auth/login/login';
import { RegisterComponent } from './pages/auth/register/register';
import { ProfileComponent } from './pages/profile/profile';
import { adminGuard } from './guards/admin-guard';
import { AdminCatsComponent } from './pages/admin/admin-cats/admin-cats';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard';
import { AdminApplicationsComponent } from './pages/admin/admin-applications/admin-applications';
import { AdminUsersComponent } from './pages/admin/admin-users/admin-users';

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
  {
    path: 'admin',
    canActivate: [adminGuard],
    children: [
      {
        path: '',
        component: AdminDashboardComponent
      },
      {
        path: 'cats',
        component: AdminCatsComponent
      },
      {
        path: 'applications',
        component: AdminApplicationsComponent
      },
      {
        path: 'users',
        component: AdminUsersComponent
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
