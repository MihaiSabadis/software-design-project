// studio-management-page.component.ts
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ReactiveFormsModule, NonNullableFormBuilder, Validators } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { StudioService } from '../../services/studio.service';
import { Studio } from '../../models/studio-model';

@Component({
  selector: 'app-studio-management-page',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatIconModule,
  ],
  templateUrl: './studio-management-page.component.html',
  styleUrl: './studio-management-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StudioManagementPageComponent {
  private readonly studioService = inject(StudioService);
  private readonly fb = inject(NonNullableFormBuilder);

  studios = signal<Studio[]>([]);
  displayedColumns = ['name', 'description', 'registrationCode', 'actions'];

  form = this.fb.group({
    name: ['', Validators.required],
    description: [''],
  });

  constructor() {
    this.load();
  }

  load() {
    this.studioService.getAll().subscribe((data) => this.studios.set(data));
  }

  create() {
    if (this.form.invalid) return;
    this.studioService.create(this.form.getRawValue()).subscribe(() => {
      this.form.reset();
      this.load();
    });
  }

  delete(id: string) {
    if (!confirm('Delete this studio and all its games?')) return;
    this.studioService.delete(id).subscribe(() => this.load());
  }
}
