// demo-app/src/app/features/video-game-list/video-game-list-page.component.ts
import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoGameService } from '../../services/video-game.service';
import { PersonService } from '../../services/person.service';
import { LoginStore } from '../login/login.store';
import { VideoGame } from '../../models/video-game.model';
import { Studio } from '../../models/studio-model';
import { VideoGameCreateDTO } from '../../models/video-game-create.dto';
import { GameCardComponent } from '../../components/video-game-card/game-card.component';
import { GameFormDialogComponent } from '../../components/video-game-form-dialog/game-form-dialog.component';

@Component({
  selector: 'app-video-game-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule, GameCardComponent, GameFormDialogComponent],
  templateUrl: './video-game-list-page.component.html',
  styleUrls: ['./video-game-list-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VideoGameListPageComponent implements OnInit {
  private readonly videoGameService = inject(VideoGameService);
  private readonly personService = inject(PersonService);
  readonly loginStore = inject(LoginStore);

  // Game list state
  games = signal<VideoGame[]>([]);
  titleSearchString = '';
  developerSearchString = '';
  maxPrice: number | null = null;
  sortBy = 'title';
  sortDir = 'asc';

  // Dialog state
  isDialogOpen = signal(false);
  gameBeingEdited = signal<VideoGame | null>(null);

  // Moderator's studio — null for admin/player
  moderatorStudio = signal<Studio | null>(null);

  get canAddGame(): boolean {
    return this.loginStore.role() === 'ADMIN' || this.loginStore.role() === 'MODERATOR';
  }

  ngOnInit(): void {
    this.loadGames();

    // If the user is a moderator, fetch their studio so we can
    // pre-fill the game form and filter edit/delete buttons.
    if (this.loginStore.role() === 'MODERATOR') {
      const userId = this.loginStore.userId();
      if (userId) {
        this.personService.getById(userId).subscribe({
          next: (person) => {
            this.moderatorStudio.set(person.studio ?? null);
          },
        });
      }
    }
  }

  openDialog(): void {
    this.gameBeingEdited.set(null);
    this.isDialogOpen.set(true);
  }

  closeDialog(): void {
    this.isDialogOpen.set(false);
    this.gameBeingEdited.set(null);
  }

  loadGames(): void {
    this.videoGameService
      .getVideoGames(
        this.titleSearchString,
        this.developerSearchString,
        this.maxPrice,
        this.sortBy,
        this.sortDir,
      )
      .subscribe({
        next: (data) => this.games.set(data),
        error: (err) => console.error(err),
      });
  }

  onFilterChange(): void {
    this.loadGames();
  }

  handleEditGame(game: VideoGame): void {
    this.gameBeingEdited.set(game);
    this.isDialogOpen.set(true);
  }

  handleSaveGame(gameData: VideoGameCreateDTO): void {
    const editing = this.gameBeingEdited();

    if (editing?.id) {
      this.videoGameService.editVideoGame(editing.id, gameData).subscribe({
        next: () => {
          this.closeDialog();
          this.loadGames();
        },
        error: (err) => {
          console.error(err);
          alert('Failed to update game.');
        },
      });
    } else {
      this.videoGameService.addVideoGame(gameData).subscribe({
        next: () => {
          this.closeDialog();
          this.loadGames();
        },
        error: (err) => {
          console.error(err);
          alert('Failed to save game.');
        },
      });
    }
  }

  handleDeleteGame(gameId: string): void {
    if (!confirm('Are you sure you want to delete this game?')) return;
    this.videoGameService.deleteVideoGame(gameId).subscribe({
      next: () => this.loadGames(),
      error: (err) => {
        console.error(err);
        alert('Could not delete the game.');
      },
    });
  }
}
