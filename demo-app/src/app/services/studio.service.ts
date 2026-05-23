import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Studio } from '../models/studio-model';

export interface StudioCreateDTO {
  name: string;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class StudioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/studios';

  getAll(): Observable<Studio[]> {
    return this.http.get<Studio[]>(this.apiUrl);
  }

  create(dto: StudioCreateDTO): Observable<Studio> {
    return this.http.post<Studio>(this.apiUrl, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
