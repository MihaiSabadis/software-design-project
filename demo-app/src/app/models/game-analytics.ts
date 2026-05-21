export interface PricePoint {
  date: string;
  price: number;
}

export interface PatchPoint {
  date: string;
  version: string;
  description: string;
}

export interface GameAnalytics {
  priceHistory: PricePoint[];
  patchHistory: PatchPoint[];
  linearPrediction: PricePoint[];
  baselinePrediction: PricePoint[];
}
