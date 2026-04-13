import { Component, Input, EventEmitter, Output } from '@angular/core';
import { VideoGame } from '../../models/video-game.model';

@Component({
  selector: 'app-video-game-card',
  imports: [],
  templateUrl: './video-game-card.component.html',
  styleUrl: './video-game-card.component.scss',
})
export class VideoGameCardComponent {
  @Input({ required: true }) game!: VideoGame;

  @Output() deleteClick = new EventEmitter<string>();
  @Output() editClick = new EventEmitter<VideoGame>();

  onDelete(): void{
    if(this.game.id){
      this.deleteClick.emit(this.game.id);
    }
  }

  onEdit():void{
    this.editClick.emit(this.game);
  }

}
