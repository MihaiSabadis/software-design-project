import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { CreatePersonDto, Person, UpdatePersonDto } from '../../models/person.model';
import { PersonService } from '../../services/person.service';

@Injectable({ providedIn: 'root' })
export class PersonListStore {
  private readonly personService = inject(PersonService);
  private readonly pendingRequests = signal(0);

  readonly persons = signal<Person[]>([]);
  // changed: Now holds a string message or null
  readonly hasError = signal<string | null>(null);
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  // helper to extract the exact message from Spring Boot
  private extractError(err: HttpErrorResponse): string {
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    // catch validation error objects
    if (err.error && typeof err.error === 'object') return JSON.stringify(err.error);
    return 'Validation failed!';
  }

  load(): void {
    this.hasError.set(null);
    this.beginRequest();
    this.personService
      .getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.persons.set(data),
        error: (err: HttpErrorResponse) => this.hasError.set(this.extractError(err)),
      });
  }

  create(dto: CreatePersonDto): void {
    this.hasError.set(null);
    this.beginRequest();
    this.personService
      .create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => this.persons.update((list) => [...list, created]),
        error: (err: HttpErrorResponse) => this.hasError.set(this.extractError(err)),
      });
  }

  update(id: string, dto: UpdatePersonDto): void {
    const existing = this.persons().find((p) => p.id === id);
    if (!existing) return;

    const payload: CreatePersonDto = { ...dto, password: existing.password };

    this.hasError.set(null);
    this.beginRequest();
    this.personService
      .update(id, payload)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) =>
          this.persons.update((list) =>
            list.map((person) => (person.id === updated.id ? updated : person)),
          ),
        error: (err: HttpErrorResponse) => this.hasError.set(this.extractError(err)),
      });
  }

  remove(id: string): void {
    this.hasError.set(null);
    this.beginRequest();
    this.personService
      .delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () =>
          this.persons.update((list) => list.filter((person) => person.id !== id)),
        error: (err: HttpErrorResponse) => this.hasError.set(this.extractError(err)),
      });
  }
}
