
export interface StoreDeal {
  storeName: string;
  price: string;
  retailPrice: string;
  savingsPercent: string;
  dealUrl: string;
}

export interface PatchNote {
  title: string;
  url: string;
  date: number; // epoch seconds
  contents: string;
}

export interface ExternalGameData {
  cheapestPriceEver: string | null;
  cheapestPriceDateEpoch: number | null;
  metacriticScore: string | null;
  steamRating: string | null;
  currentDeals: StoreDeal[];
  recentUpdates: PatchNote[];
  description: string | null;
  backgroundImage: string | null;
  genres: string[];
  releaseDate: string | null;
}
