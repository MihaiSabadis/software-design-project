import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VideoGameService } from '../../services/video-game.service';
import { ReviewService } from '../../services/review.service';
import { VideoGame } from '../../models/video-game.model';
import { Review } from '../../models/review.model';
import { ReviewCreateDTO } from '../../models/review-create.dto';

@Component({
  selector: 'app-video-game-details-page',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink], // Notice RouterLink for the 'Back' button!
  templateUrl: './video-game-details-page.component.html',
  styleUrls: ['./video-game-details-page.component.scss'],
})
export class VideoGameDetailsPageComponent implements OnInit {
  // Services
  private readonly route = inject(ActivatedRoute);
  private readonly videoGameService = inject(VideoGameService);
  private readonly reviewService = inject(ReviewService);

  // State Variables
  gameId: string | null = null;
  game: VideoGame | null = null;
  reviews: Review[] = [];
  isEditing: boolean = false;

  // Simulated Authentication (Change this name to test different users!)
  currentUser: string = '851fc701-d89e-4c33-802f-eea95a0cc929';

  // Smart Area Variables
  userReview: Review | undefined;
  newReview: ReviewCreateDTO = { authorId: this.currentUser, score: 5, comment: '' };

  ngOnInit(): void {
    // 1. Read the ID from the URL (e.g., /games/123)
    this.gameId = this.route.snapshot.paramMap.get('id');

    if (this.gameId) {
      this.loadGame();
      this.loadReviews();
    }
  }

  loadGame(): void {
    this.videoGameService.getVideoGameById(this.gameId!).subscribe({
      next: (data) => (this.game = data),
      error: (err) => console.error('Failed to load game', err),
    });
  }

  loadReviews(): void {
    this.reviewService.getReviewsForGame(this.gameId!).subscribe({
      next: (data) => {
        this.reviews = data;
        this.userReview = this.reviews.find((r) => r.authorId === this.currentUser);
      },
      error: (err) => console.error('Failed to load reviews', err),
    });
  }

  submitReview(): void {
    if (this.newReview.comment.trim()) {
      this.newReview.gameId = this.gameId!;
      this.newReview.authorId = this.currentUser;
      this.reviewService.addReview(this.gameId!, this.newReview).subscribe({
        next: () => {
          this.loadReviews(); // Refresh the list, which will hide the form automatically!
          this.newReview.comment = ''; // Clear the box
        },
        error: (err) => alert('Failed to post review. You might have already reviewed this!'),
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
        error: (err) => console.error('Failed to delete', err),
      });
    }
  }

  startEditing(): void {
    this.isEditing = true;
    // Pre-fill the form suitcase with the exact text and score they already saved!
    this.newReview = {
      authorId: this.currentUser,
      gameId: this.gameId!,
      score: this.userReview!.score,
      comment: this.userReview!.comment,
    };
  }

  cancelEditing(): void {
    this.isEditing = false;
    // Wipe the form clean just in case
    this.newReview = { authorId: this.currentUser, score: 5, comment: '' };
  }

  submitEdit(): void {
    if (this.newReview.comment.trim() && this.userReview?.id) {
      // Send the updated suitcase to the backend!
      this.reviewService.updateReview(this.userReview.id, this.newReview).subscribe({
        next: () => {
          this.isEditing = false; // Turn off edit mode
          this.loadReviews(); // Refresh the page to see the new text!
        },
        error: (err) => console.error('Failed to update review', err),
      });
    }
  }
}
