import {Studio} from './studio-model';

export interface VideoGame {
  id?: string;
  title: string;
  price: number;
  studio: Studio;
  coverImageUrl?: string;
}
