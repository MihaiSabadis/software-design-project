// demo-app/src/app/components/navbar/navbar.component.ts
import { ChangeDetectionStrategy, Component, inject, computed } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { LoginStore } from '../../features/login/login.store';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent {
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  protected readonly themeService = inject(ThemeService);

  protected readonly role = this.loginStore.role;
  protected readonly isAdmin = computed(() => this.loginStore.role() === 'ADMIN');
  protected readonly isMod = computed(() => this.loginStore.role() === 'MODERATOR');
  protected readonly isPlayer = computed(() => this.loginStore.role() === 'PLAYER');

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }
}
