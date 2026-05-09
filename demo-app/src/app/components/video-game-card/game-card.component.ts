// demo-app/src/app/components/video-game-card/game-card.component.ts
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  EventEmitter,
  inject,
  input,
  Output,
} from '@angular/core';
import { VideoGame } from '../../models/video-game.model';
import { RouterLink } from '@angular/router';
import { LoginStore } from '../../features/login/login.store';

@Component({
  selector: 'app-video-game-card',
  imports: [RouterLink],
  templateUrl: './game-card.component.html',
  styleUrl: './game-card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameCardComponent {
  readonly game = input.required<VideoGame>();
  // The studio ID of the logged-in moderator (null for admin/player)
  readonly moderatorStudioId = input<string | null>(null);

  readonly loginStore = inject(LoginStore);

  @Output() deleteClick = new EventEmitter<string>();
  @Output() editClick = new EventEmitter<VideoGame>();

  protected readonly canManage = computed(() => {
    const role = this.loginStore.role();
    if (role === 'ADMIN') return true;
    if (role === 'MODERATOR') {
      const sid = this.moderatorStudioId();
      return sid != null && this.game().studio?.id === sid;
    }
    return false;
  });

  protected onDelete(): void {
    const id = this.game().id;
    if (id) this.deleteClick.emit(id);
  }

  protected onEdit(): void {
    this.editClick.emit(this.game());
  }
}
