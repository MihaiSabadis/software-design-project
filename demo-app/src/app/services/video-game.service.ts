import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { VideoGame } from '../models/video-game.model';
import {VideoGameCreateDTO} from "../models/video-game-create.dto";

@Injectable({ providedIn: 'root' })
export class VideoGameService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/videogames';

  addVideoGame(game: VideoGameCreateDTO): Observable<VideoGame> {
    return this.http.post<VideoGame>(this.apiUrl, game);
  }

  getVideoGames(
    title?: string,
    developer?: string,
    maxPrice?: number | null,
    sortBy: string = 'title',
    sortDir: string = 'asc',
  ): Observable<VideoGame[]> {
    let params = new HttpParams().set('sortBy', sortBy).set('sortDir', sortDir);

    if (title) params = params.set('title', title);
    if (developer) params = params.set('developer', developer);
    if (maxPrice) params = params.set('maxPrice', maxPrice.toString());

    return this.http.get<VideoGame[]>(this.apiUrl, { params });
  }

  getVideoGameById(id: string): Observable<VideoGame>{
    return this.http.get<VideoGame>(`${this.apiUrl}/${id}`);
  }

  deleteVideoGame(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  editVideoGame(id: string, game: VideoGameCreateDTO): Observable<VideoGame> {
    return this.http.put<VideoGame>(`${this.apiUrl}/${id}`, game);
  }
}
