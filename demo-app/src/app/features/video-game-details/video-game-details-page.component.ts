import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VideoGameService } from '../../services/video-game.service';
import { ReviewService } from '../../services/review.service';
import { PersonService } from '../../services/person.service';
import { PatchService, PatchCreateDTO, PatchResponseDTO } from '../../services/patch.service';
import { VideoGame } from '../../models/video-game.model';
import { Review } from '../../models/review.model';
import { ReviewCreateDTO } from '../../models/review-create.dto';
import { LoginStore } from '../login/login.store';
import { GameAnalyticsComponent } from '../../components/game-analytics/game-analytics.component';

@Component({
  selector: 'app-video-game-details-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, GameAnalyticsComponent],
  templateUrl: './video-game-details-page.component.html',
  styleUrls: ['./video-game-details-page.component.scss'],
})
export class VideoGameDetailsPageComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly route = inject(ActivatedRoute);
  private readonly videoGameService = inject(VideoGameService);
  private readonly reviewService = inject(ReviewService);
  private readonly personService = inject(PersonService);
  private readonly patchService = inject(PatchService);
  readonly loginStore = inject(LoginStore);

  gameId: string | null = null;
  game: VideoGame | null = null;
  reviews: Review[] = [];
  userReview: Review | undefined;
  isEditing = false;
  isOwned = false;
  currentUser = '';

  // Patch state
  patches: PatchResponseDTO[] = [];
  isPatchFormOpen = false;
  isPatchSubmitting = false;
  patchError: string | null = null;
  newPatch: PatchCreateDTO = { version: '', description: '', releaseDate: '' };

  newReview: ReviewCreateDTO = { authorId: '', score: 5, comment: '' };

  ngOnInit(): void {
    this.currentUser = this.loginStore.userId() ?? '';
    this.newReview.authorId = this.currentUser;

    this.route.paramMap.subscribe((params) => {
      this.gameId = params.get('id');
      this.game = null;
      this.reviews = [];
      this.userReview = undefined;
      this.patches = [];

      if (this.gameId) {
        this.loadGame();
        this.loadReviews();
        this.loadPatches();
      }
    });
  }

  loadGame(): void {
    this.videoGameService.getVideoGameById(this.gameId!).subscribe({
      next: (data) => {
        this.game = data;
        if (this.currentUser) {
          this.personService.getById(this.currentUser).subscribe({
            next: (person) => {
              this.isOwned = !!person.ownedGames?.some((g) => g.id === this.gameId);
              this.cdr.detectChanges();
            },
          });
        }
        this.cdr.detectChanges();
      },
    });
  }

  addToLibrary(): void {
    if (this.gameId && this.currentUser) {
      this.personService.addGameToLibrary(this.currentUser, this.gameId).subscribe({
        next: () => {
          this.isOwned = true;
          this.cdr.detectChanges();
        },
        error: () => alert('Failed to add game to library.'),
      });
    }
  }

  loadReviews(): void {
    this.reviewService.getReviewsForGame(this.gameId!).subscribe({
      next: (data) => {
        this.reviews = data;
        this.userReview = this.reviews.find((r) => r.authorId === this.currentUser);
        this.cdr.detectChanges();
      },
    });
  }

  submitReview(): void {
    if (!this.newReview.comment.trim()) return;
    this.newReview.gameId = this.gameId!;
    this.newReview.authorId = this.currentUser;
    this.reviewService.addReview(this.gameId!, this.newReview).subscribe({
      next: () => {
        this.loadReviews();
        this.newReview.comment = '';
      },
      error: () => alert('Could not post review. You may have already reviewed this game.'),
    });
  }

  deleteMyReview(): void {
    if (this.userReview?.id && confirm('Delete your review?')) {
      this.reviewService.deleteReview(this.userReview.id).subscribe({
        next: () => {
          this.userReview = undefined;
          this.loadReviews();
        },
      });
    }
  }

  startEditing(): void {
    this.isEditing = true;
    this.newReview = {
      authorId: this.currentUser,
      gameId: this.gameId!,
      score: this.userReview!.score,
      comment: this.userReview!.comment,
    };
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.newReview = { authorId: this.currentUser, score: 5, comment: '' };
  }

  submitEdit(): void {
    if (!this.newReview.comment.trim() || !this.userReview?.id) return;
    this.reviewService.updateReview(this.userReview.id, this.newReview).subscribe({
      next: () => {
        this.isEditing = false;
        this.loadReviews();
      },
    });
  }

  get canManagePatches(): boolean {
    const role = this.loginStore.role();
    return role === 'ADMIN' || role === 'MODERATOR';
  }

  loadPatches(): void {
    this.patchService.getPatches(this.gameId!).subscribe({
      next: (data) => {
        this.patches = data;
        this.cdr.detectChanges();
      },
    });
  }

  openPatchForm(): void {
    this.newPatch = { version: '', description: '', releaseDate: '' };
    this.patchError = null;
    this.isPatchFormOpen = true;
  }

  closePatchForm(): void {
    this.isPatchFormOpen = false;
    this.patchError = null;
  }

  submitPatch(): void {
    if (!this.newPatch.version.trim() || !this.newPatch.releaseDate) {
      this.patchError = 'Version and release date are required.';
      return;
    }
    this.isPatchSubmitting = true;
    this.patchError = null;

    this.patchService.addPatch(this.gameId!, this.newPatch).subscribe({
      next: () => {
        this.isPatchSubmitting = false;
        this.isPatchFormOpen = false;
        this.loadPatches();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isPatchSubmitting = false;
        this.patchError = err?.error?.message ?? 'Failed to add patch.';
        this.cdr.detectChanges();
      },
    });
  }

  deletePatch(patchId: string): void {
    if (!confirm('Delete this patch entry?')) return;
    this.patchService.deletePatch(this.gameId!, patchId).subscribe({
      next: () => {
        this.loadPatches();
        this.cdr.detectChanges();
      },
    });
  }
}
