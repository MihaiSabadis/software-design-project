import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface VideoGame {
  id?: string;
  title: string;
  price: number;
  developer: string;
}

@Injectable({ providedIn: 'root' })
export class VideoGameService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/videogames';

  // 1. GET (Read) - cu filtrele de la Tema 2
  getGames(title?: string, developer?: string, maxPrice?: number): Observable<VideoGame[]> {
    let params = new HttpParams();
    if (title) params = params.set('title', title);
    if (developer) params = params.set('developer', developer);
    if (maxPrice) params = params.set('maxPrice', maxPrice.toString());

    return this.http.get<VideoGame[]>(this.apiUrl, { params });
  }

  deleteGame(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // (Metodele de POST și PUT le vei folosi mai târziu când faci modalul de adăugare)
}
