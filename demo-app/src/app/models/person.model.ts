export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  password: string;
  role: 'ADMIN' | 'PLAYER';
}

export type CreatePersonDto = Omit<Person, 'id'>;
export type UpdatePersonDto = Omit<Person, 'id'>;

