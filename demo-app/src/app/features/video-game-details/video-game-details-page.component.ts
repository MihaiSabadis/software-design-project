import { Component, inject, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VideoGameService } from '../../services/video-game.service';
import { ReviewService } from '../../services/review.service';
import { PersonService } from '../../services/person.service';
import { VideoGame } from '../../models/video-game.model';
import { Review } from '../../models/review.model';
import { ReviewCreateDTO } from '../../models/review-create.dto';
import { LoginStore } from '../login/login.store';

@Component({
  selector: 'app-video-game-details-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './video-game-details-page.component.html',
  styleUrls: ['./video-game-details-page.component.scss'],
})
export class VideoGameDetailsPageComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);

  // Services
  private readonly route = inject(ActivatedRoute);
  private readonly videoGameService = inject(VideoGameService);
  private readonly reviewService = inject(ReviewService);
  private readonly personService = inject(PersonService);
  private readonly loginStore = inject(LoginStore);

  // State Variables
  gameId: string | null = null;
  game: VideoGame | null = null;
  reviews: Review[] = [];
  isEditing: boolean = false;
  isOwned: boolean = false;

  // 1. Initialize as an empty string instead of a hardcoded ID
  currentUser: string = '';

  // 2. Initialize with an empty authorId (we will fill it in ngOnInit)
  newReview: ReviewCreateDTO = { authorId: '', score: 5, comment: '' };

  ngOnInit(): void {
    // 3. Grab the real logged-in User ID from the store!
    // We use || '' as a fallback just in case it is null
    this.currentUser = this.loginStore.userId() || '';
    this.newReview.authorId = this.currentUser;

    this.route.paramMap.subscribe((params) => {
      this.gameId = params.get('id');

      this.game = null;
      this.reviews = [];
      this.userReview = undefined;

      if (this.gameId) {
        this.loadGame();
        this.loadReviews();
      }
    });
  }

  // Smart Area Variables
  userReview: Review | undefined;

  loadGame(): void {
    this.videoGameService.getVideoGameById(this.gameId!).subscribe({
      next: (data) => {
        this.game = data;

        // Make sure we only fetch the profile if we have a valid logged-in user
        if (this.currentUser) {
          this.personService.getById(this.currentUser).subscribe({
            next: (person) => {
              this.isOwned = !!person.ownedGames?.some((g) => g.id === this.gameId);
              this.cdr.detectChanges();
            },
            error: (err) => console.error('Could not fetch user profile', err),
          });
        }

        this.cdr.detectChanges();
      },
      error: (err) => console.error(err),
    });
  }

  loadReviews(): void {
    this.reviewService.getReviewsForGame(this.gameId!).subscribe({
      next: (data) => {
        this.reviews = data;
        this.userReview = this.reviews.find((r) => r.authorId === this.currentUser);
        this.cdr.detectChanges();
      },
      error: (err) => console.error(err),
    });
  }

  submitReview(): void {
    if (this.newReview.comment.trim()) {
      this.newReview.gameId = this.gameId!;
      this.newReview.authorId = this.currentUser;
      this.reviewService.addReview(this.gameId!, this.newReview).subscribe({
        next: () => {
          this.loadReviews();
          this.newReview.comment = '';
        },
        error: () => alert('Failed to post review. You might have already reviewed this!'),
      });
    }
  }

  deleteMyReview(): void {
    if (this.userReview && this.userReview.id && confirm('Delete your review?')) {
      this.reviewService.deleteReview(this.userReview.id).subscribe({
        next: () => {
          this.userReview = undefined;
          this.loadReviews();
        },
        error: (err) => console.error(err),
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
    if (this.newReview.comment.trim() && this.userReview?.id) {
      this.reviewService.updateReview(this.userReview.id, this.newReview).subscribe({
        next: () => {
          this.isEditing = false;
          this.loadReviews();
        },
        error: (err) => console.error(err),
      });
    }
  }

  addToLibrary(): void {
    if (this.gameId && this.currentUser) {
      this.personService.addGameToLibrary(this.currentUser, this.gameId).subscribe({
        next: () => {
          this.isOwned = true;
          this.cdr.detectChanges();
        },
        error: (err) => alert('Failed to add game to library.'),
      });
    }
  }
}
