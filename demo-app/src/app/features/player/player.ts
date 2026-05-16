// demo-app/src/app/features/player/player.ts
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PersonService } from '../../services/person.service';
import { LoginStore } from '../login/login.store';
import { VideoGame } from '../../models/video-game.model';
import { GameCardComponent } from '../../components/video-game-card/game-card.component';

@Component({
  selector: 'app-player',
  imports: [RouterLink, GameCardComponent],
  templateUrl: './player.html',
  styleUrl: './player.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Player implements OnInit {
  private readonly personService = inject(PersonService);
  private readonly loginStore = inject(LoginStore);

  protected readonly ownedGames = signal<VideoGame[]>([]);
  protected readonly isLoading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly removeError = signal<string | null>(null);
  protected readonly removingId = signal<string | null>(null);

  ngOnInit(): void {
    this.loadLibrary();
  }

  private loadLibrary(): void {
    const userId = this.loginStore.userId();
    if (!userId) {
      this.isLoading.set(false);
      return;
    }

    this.personService.getById(userId).subscribe({
      next: (person) => {
        this.ownedGames.set(person.ownedGames ?? []);
        this.isLoading.set(false);
      },
      error: () => {
        this.loadError.set('Could not load your library.');
        this.isLoading.set(false);
      },
    });
  }

  protected handleRemove(gameId: string): void {
    const userId = this.loginStore.userId();
    if (!userId) return;

    this.removeError.set(null);
    this.removingId.set(gameId);

    this.personService.removeGameFromLibrary(userId, gameId).subscribe({
      next: (updatedPerson) => {
        this.ownedGames.set(updatedPerson.ownedGames ?? []);
        this.removingId.set(null);
      },
      error: (err) => {
        this.removeError.set(err?.error?.message ?? 'Delete your review for this game first.');
        this.removingId.set(null);
      },
    });
  }
}
