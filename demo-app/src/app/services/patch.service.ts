
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface PatchCreateDTO {
  version: string;
  description: string;
  releaseDate: string; // YYYY-MM-DD
}

export interface PatchResponseDTO {
  id: string;
  version: string;
  description: string;
  releaseDate: string;
  videoGameId: string;
}

@Injectable({ providedIn: 'root' })
export class PatchService {
  private readonly http = inject(HttpClient);
  private readonly base = 'http://localhost:8080/videogames';

  getPatches(gameId: string): Observable<PatchResponseDTO[]> {
    return this.http.get<PatchResponseDTO[]>(`${this.base}/${gameId}/patches`);
  }

  addPatch(gameId: string, dto: PatchCreateDTO): Observable<PatchResponseDTO> {
    return this.http.post<PatchResponseDTO>(`${this.base}/${gameId}/patches`, dto);
  }

  deletePatch(gameId: string, patchId: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${gameId}/patches/${patchId}`);
  }
}
