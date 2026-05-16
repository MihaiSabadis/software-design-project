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
  readonly moderatorStudioId = input<string | null>(null);
  readonly showRemoveButton = input<boolean>(false);
  readonly isRemoving = input<boolean>(false);

  readonly loginStore = inject(LoginStore);

  @Output() deleteClick = new EventEmitter<string>();
  @Output() editClick = new EventEmitter<VideoGame>();
  @Output() removeClick = new EventEmitter<string>();

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

  protected onRemove(): void {
    const id = this.game().id;
    if (id) this.removeClick.emit(id);
  }
}
