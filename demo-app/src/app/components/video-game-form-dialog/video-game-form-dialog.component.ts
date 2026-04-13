import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoGameCreateDTO } from '../../models/video-game-create.dto';
import { VideoGame } from '../../models/video-game.model';

@Component({
  selector: 'app-video-game-form-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './video-game-form-dialog.component.html',
  styleUrls: ['./video-game-form-dialog.component.scss'],
})
export class VideoGameFormDialogComponent {
  @Output() closeDialog = new EventEmitter<void>();
  @Output() saveGame = new EventEmitter<VideoGameCreateDTO>();

  @Input() gameToEdit: VideoGame | null = null;

  newGame: VideoGameCreateDTO = {
    title: '',
    price: 0,
    developer: '',
  };

  ngOnInit(): void {
    if (this.gameToEdit) {
      // If we are editing, copy the existing data into the form!
      this.newGame = {
        title: this.gameToEdit.title,
        developer: this.gameToEdit.developer,
        price: this.gameToEdit.price,
      };
    }
  }

  onCancel() {
    this.closeDialog.emit();
  }

  onSubmit() {
    if (this.newGame.title && this.newGame.developer) {
      this.saveGame.emit(this.newGame);
    } else {
      alert('Please fill out all fields!');
    }
  }
}
