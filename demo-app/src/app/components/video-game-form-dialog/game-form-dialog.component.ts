import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { VideoGameCreateDTO } from '../../models/video-game-create.dto';
import { VideoGame } from '../../models/video-game.model';

@Component({
  selector: 'app-video-game-form-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './game-form-dialog.component.html',
  styleUrls: ['./game-form-dialog.component.scss'],
})
export class GameFormDialogComponent {
  @Output() closeDialog = new EventEmitter<void>();
  @Output() saveGame = new EventEmitter<VideoGameCreateDTO>();

  @Input() gameToEdit: VideoGame | null = null;

  newGame: VideoGameCreateDTO = {
    title: '',
    price: 0,
    developer: '',
    coverImageUrl: ''
  };

  ngOnInit(): void {
    if (this.gameToEdit) {
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
