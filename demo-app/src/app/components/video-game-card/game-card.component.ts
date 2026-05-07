import { Component, Input, EventEmitter, Output, inject } from '@angular/core';
import { VideoGame } from '../../models/video-game.model';
import {RouterLink} from "@angular/router";
import {LoginStore} from "../../features/login/login.store";

@Component({
  selector: 'app-video-game-card',
  imports: [RouterLink],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.scss',
})
export class GameCardComponent {
  @Input({ required: true }) game!: VideoGame;

  readonly loginStore = inject(LoginStore);

  @Output() deleteClick = new EventEmitter<string>();
  @Output() editClick = new EventEmitter<VideoGame>();

  onDelete(): void {
    if (this.game.id) {
      this.deleteClick.emit(this.game.id);
    }
  }

  onEdit(): void {
    this.editClick.emit(this.game);
  }
}
