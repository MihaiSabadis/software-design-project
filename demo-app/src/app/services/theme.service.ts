import { Injectable, signal, effect } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  // Signal to track the current theme (default to dark)
  readonly isDarkMode = signal<boolean>(true);

  constructor() {
    // 1. Check local storage on initialization
    const savedTheme = localStorage.getItem('app-theme');
    if (savedTheme === 'light') {
      this.isDarkMode.set(false);
    }

    // 2. React to signal changes: Update DOM and LocalStorage
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
