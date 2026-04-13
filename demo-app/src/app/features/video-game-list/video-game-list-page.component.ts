import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoGameService } from '../../services/video-game.service';
import { VideoGame } from '../../models/video-game.model';
import { VideoGameCardComponent } from '../../components/video-game-card/video-game-card.component';
import { VideoGameCreateDTO } from "../../models/video-game-create.dto";
import { VideoGameFormDialogComponent } from "../../components/video-game-form-dialog/video-game-form-dialog.component";

@Component({
  selector: 'app-video-game-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule, VideoGameCardComponent, VideoGameFormDialogComponent], // FormsModule is required for [(ngModel)]
  templateUrl: './video-game-list-page.component.html',
  styleUrls: ['./video-game-list-page.component.scss'],
})
export class VideoGameListPageComponent implements OnInit {
  games: VideoGame[] = [];

  // State variables bound to the HTML inputs
  developerSearchString: string = '';
  maxPrice: number | null = null;
  titleSearchString: string = '';
  sortBy: string = 'title';
  sortDir: string = 'asc';

  isDialogOpen: boolean = false;

  gameBeingEdited: VideoGame | null = null;

  private readonly videoGameService = inject(VideoGameService);
  private readonly cdr = inject(ChangeDetectorRef);

  ngOnInit(): void {
    // Load all games when the page first opens
    this.loadGames();
  }

  openDialog(): void {
    this.gameBeingEdited = null;
    this.isDialogOpen = true;
  }

  closeDialog(): void {
    this.isDialogOpen = false;
  }

  loadGames(): void {
    // Passes the current state variables to your service
    this.videoGameService.getVideoGames(
        this.titleSearchString,
        this.developerSearchString,
        this.maxPrice,
        this.sortBy,
        this.sortDir).subscribe({
      next: (data) => {
        this.games = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching filtered games:', err);
      },
    });
  }

  onFilterChange(): void {
    // Triggered by the "Apply Filters" button
    this.loadGames();
    this.cdr.detectChanges();
  }

  handleEditGame(game: VideoGame): void {
    this.gameBeingEdited = game;
    this.isDialogOpen = true;
  }

  handleSaveGame(gameData: VideoGameCreateDTO): void {
    if (this.gameBeingEdited && this.gameBeingEdited.id) {
      this.videoGameService.editVideoGame(this.gameBeingEdited.id, gameData).subscribe({
        next: () => {
          this.closeDialog();
          this.loadGames();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Failed to update', err);
          alert('Failed to update game.');
        },
      });
    } else {
      this.videoGameService.addVideoGame(gameData).subscribe({
        next: () => {
          this.closeDialog();
          this.cdr.detectChanges();
          this.loadGames();
        },
        error: (err) => {
          console.error('Failed to create game', err);
          alert('Failed to save the game to the database');
        },
      });
    }
  }

  handleDeleteGame(gameId: string): void {
    const isConfirmed = window.confirm('Are you sure you want to delete this game?');

    if (isConfirmed) {
      this.videoGameService.deleteVideoGame(gameId).subscribe({
        next: () => {
          this.cdr.detectChanges();
          this.loadGames();
        },
        error: (err) => {
          console.error('Failed to delete game', err);
          alert('Could not delete the game. It might be tied to existing reviews ?');
        },
      });
    }
  }
}
