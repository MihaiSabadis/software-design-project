import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginStore } from '../features/login/login.store';

export const authGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  return loginStore.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

export const guestGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  if (loginStore.isAuthenticated()) {
    if (loginStore.role() === 'ADMIN') {
      return router.parseUrl('/people');
    } else {
      return router.createUrlTree(['/player']);
    }
  }
  //return loginStore.isAuthenticated() ? router.createUrlTree(['/people']) : true;
  return true;
};

export const adminGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  if (!loginStore.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  if (loginStore.role() === 'ADMIN') {
    return true;
  }

  return router.parseUrl('/player');
};
