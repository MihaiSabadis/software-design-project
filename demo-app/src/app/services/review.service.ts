import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Review } from '../models/review.model';
import { ReviewCreateDTO } from '../models/review-create.dto';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/reviews';

  getReviewsForGame(gameId: string): Observable<Review[]> {
    return this.http.get<Review[]>(`${this.apiUrl}/game/${gameId}`);
  }

  addReview(gameId: string, reviewData: ReviewCreateDTO): Observable<Review> {
    reviewData.gameId = gameId;

    return this.http.post<Review>(this.apiUrl, reviewData);
  }

  updateReview(reviewId: string, reviewData: ReviewCreateDTO): Observable<Review> {
    return this.http.put<Review>(`${this.apiUrl}/${reviewId}`, reviewData);
  }

  deleteReview(reviewId: string): Observable<String> {
    return this.http.delete(`${this.apiUrl}/${reviewId}`, {responseType: 'text'});
  }
}
