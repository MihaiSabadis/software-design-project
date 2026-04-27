import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { LoginStore } from './features/login/login.store';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const loginStore = inject(LoginStore);
  const token = loginStore.token();

  const isLogin = request.url.endsWith('/login');
  const isRegistration = request.url.endsWith('/person') && request.method === 'POST';

  if (!token || isLogin || isRegistration) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
