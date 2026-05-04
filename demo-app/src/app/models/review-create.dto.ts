export interface ReviewCreateDTO{
  authorId: string;
  score: number;
  comment: string;
  gameId?: string;
}
