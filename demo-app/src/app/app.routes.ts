import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'people',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/person-list/person-list-page.component').then(
        (m) => m.PersonListPageComponent,
      ),
  },
  {
    path: 'player',
    canActivate: [authGuard],
    loadComponent: () => import('./features/player/player').then((m) => m.Player),
  },
  {
    path: 'games',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/video-game-list/video-game-list-page.component').then(
        (m) => m.VideoGameListPageComponent,
      ),
  },
  {
    path: 'games/:id',
    loadComponent: () =>
      import('./features/video-game-details/video-game-details-page.component').then(
        (m) => m.VideoGameDetailsPageComponent,
      ),
  },
  {
    path: 'forgot-password',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/password-reset/password-reset.component').then(
        (m) => m.PasswordResetComponent,
      ),
  },
  {
    path: 'error',
    loadComponent: () =>
      import('./features/not-found/not-found-page.component').then((m) => m.NotFoundPageComponent),
  },
  {
    path: '**',
    redirectTo: 'error',
  },
];
