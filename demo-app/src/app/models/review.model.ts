export interface Review {
  id?: string;
  authorId: string;
  authorName?: string;
  score: number;
  comment: string;
  gameId: string;
}
