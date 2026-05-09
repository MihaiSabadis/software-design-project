import { VideoGame } from './video-game.model';
import { Studio } from './studio-model';

export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  password: string;
  role: 'ADMIN' | 'PLAYER' | 'MODERATOR';
  studio?: Studio
  ownedGames?: VideoGame[];
}

export type CreatePersonDto = Omit<Person, 'id'>;
export type UpdatePersonDto = Omit<Person, 'id'>;

