import { VideoGame } from './video-game.model';

export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  password: string;
  role: 'ADMIN' | 'PLAYER';
  ownedGames?: VideoGame[];
}

export type CreatePersonDto = Omit<Person, 'id'>;
export type UpdatePersonDto = Omit<Person, 'id'>;

