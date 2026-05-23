import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  readonly isDarkMode = signal<boolean>(true);

  constructor() {
    const savedTheme = localStorage.getItem('app-theme');
    if (savedTheme === 'light') {
      this.isDarkMode.set(false);
    }

    effect(() => {
      const dark = this.isDarkMode();
      if (dark) {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('app-theme', 'dark');
      } else {
        document.documentElement.setAttribute('data-theme', 'light');
        localStorage.setItem('app-theme', 'light');
      }
    });
  }

  toggleTheme(): void {
    this.isDarkMode.update((dark) => !dark);
  }
}
