import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoGameCreateDTO } from '../../models/video-game-create.dto';
import { VideoGame } from '../../models/video-game.model';
import { Studio } from '../../models/studio-model';
import { StudioService } from '../../services/studio.service';

@Component({
  selector: 'app-video-game-form-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './game-form-dialog.component.html',
  styleUrls: ['./game-form-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameFormDialogComponent implements OnInit {
  @Output() closeDialog = new EventEmitter<void>();
  @Output() saveGame = new EventEmitter<VideoGameCreateDTO>();

  @Input() gameToEdit: VideoGame | null = null;
  @Input() moderatorStudio: Studio | null = null;

  private readonly studioService = inject(StudioService);

  studios = signal<Studio[]>([]);

  form: VideoGameCreateDTO = {
    title: '',
    price: 0,
    studioId: '',
    coverImageUrl: '',
  };

  get studioLocked(): boolean {
    return this.moderatorStudio !== null || this.gameToEdit !== null;
  }

  get studioDisplayName(): string {
    if (this.moderatorStudio) return this.moderatorStudio.name;
    if (this.gameToEdit?.studio) return this.gameToEdit.studio.name;
    return '';
  }

  ngOnInit(): void {
    if (this.gameToEdit) {
      this.form = {
        title: this.gameToEdit.title,
        price: this.gameToEdit.price,
        studioId: this.gameToEdit.studio?.id ?? '',
        coverImageUrl: this.gameToEdit.coverImageUrl ?? '',
      };
    } else if (this.moderatorStudio) {
      this.form.studioId = this.moderatorStudio.id ?? '';
    }

    if (!this.studioLocked) {
      this.studioService.getAll().subscribe({
        next: (data) => this.studios.set(data),
      });
    }
  }

  onCancel(): void {
    this.closeDialog.emit();
  }

  onSubmit(): void {
    if (!this.form.title.trim()) {
      alert('Title is required.');
      return;
    }
    if (!this.form.studioId) {
      alert('A studio is required.');
      return;
    }
    this.saveGame.emit({ ...this.form });
  }
}
