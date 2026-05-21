// demo-app/src/app/components/game-analytics/game-analytics.component.ts
import {
  ChangeDetectionStrategy,
  Component,
  ViewChild,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, Chart, registerables } from 'chart.js';
import annotationPlugin from 'chartjs-plugin-annotation';
import { VideoGameService } from '../../services/video-game.service';
import { ReviewService } from '../../services/review.service';
import { Review } from '../../models/review.model';
import { GameAnalytics } from '../../models/game-analytics';
import { ExternalGameData, StoreDeal } from '../../models/external-game-data.model';
import 'chartjs-adapter-date-fns';

Chart.register(...registerables, annotationPlugin);

type ScoreTier = 'high' | 'medium' | 'low' | 'none';

interface SortedDeal extends StoreDeal {
  isBestDeal: boolean;
}

@Component({
  selector: 'app-game-analytics',
  imports: [BaseChartDirective, CommonModule],
  templateUrl: './game-analytics.component.html',
  styleUrls: ['./game-analytics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameAnalyticsComponent {
  readonly gameId = input.required<string | undefined>();

  private readonly gameService = inject(VideoGameService);
  private readonly reviewService = inject(ReviewService);

  @ViewChild(BaseChartDirective) private chart?: BaseChartDirective;

  // ── State ─────────────────────────────────────────────────────────────
  protected readonly isChartLoaded = signal(false);
  protected readonly isReviewChartLoaded = signal(false);
  protected readonly externalData = signal<ExternalGameData | null>(null);
  protected readonly isExternalLoaded = signal(false);

  protected readonly hasAnyExternalData = computed(() => {
    const ext = this.externalData();
    if (!ext) return false;
    return !!(
      ext.cheapestPriceEver ||
      ext.metacriticScore ||
      ext.steamRating ||
      (ext.currentDeals && ext.currentDeals.length > 0)
    );
  });

  protected readonly metacriticTier = computed<ScoreTier>(() => {
    const score = Number(this.externalData()?.metacriticScore);
    if (!Number.isFinite(score)) return 'none';
    if (score >= 75) return 'high';
    if (score >= 50) return 'medium';
    return 'low';
  });

  protected readonly steamTier = computed<ScoreTier>(() => {
    const rating = this.externalData()?.steamRating?.toLowerCase() ?? '';
    if (/overwhelmingly positive|very positive/.test(rating)) return 'high';
    if (/mostly positive|positive/.test(rating)) return 'high';
    if (/mixed/.test(rating)) return 'medium';
    if (/negative/.test(rating)) return 'low';
    return 'none';
  });

  protected readonly sortedDeals = computed<SortedDeal[]>(() => {
    const deals = this.externalData()?.currentDeals ?? [];
    if (deals.length === 0) return [];
    const sorted = [...deals].sort((a, b) => parseFloat(a.price) - parseFloat(b.price));
    const cheapest = parseFloat(sorted[0].price);
    return sorted.map((d) => ({
      ...d,
      isBestDeal: parseFloat(d.price) === cheapest,
    }));
  });

  // ── Chart config ──────────────────────────────────────────────────────

  barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: ['1 Star', '2 Stars', '3 Stars', '4 Stars', '5 Stars'],
    datasets: [
      {
        data: [0, 0, 0, 0, 0], // This will hold our review counts
        label: 'Reviews',
        // Optional: Color code the bars from red (bad) to green (good)
        backgroundColor: ['#ff4d4f', '#ff7a45', '#ffa940', '#fadb14', '#00d6a0'],
        borderRadius: 4, // Rounded corners on the bars
      },
    ],
  };

  barChartOptions: ChartOptions<'bar'> = {
    responsive: true,
    maintainAspectRatio: false,
    color: '#ffffff',
    scales: {
      x: {
        grid: { display: false }, // Hide vertical grid lines for a cleaner look
        ticks: { color: '#a497c6' },
      },
      y: {
        grid: { color: 'rgba(255,255,255,0.06)' },
        ticks: {
          color: '#a497c6',
          stepSize: 1, // Reviews are whole numbers, so steps should be 1
        },
      },
    },
    plugins: {
      legend: { display: false }, // Hide the legend since we only have one dataset
    },
  };

  protected lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Price ($)',
        fill: true,
        tension: 0.35,
        borderColor: '#00d6a0',
        backgroundColor: 'rgba(0, 214, 160, 0.08)',
        pointBackgroundColor: '#00d6a0',
        pointBorderColor: '#0d0820',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 7,
      },
      {
        data: [],
        label: 'Predicted Trend',
        fill: false,
        tension: 0.35,
        borderColor: '#ff07de',
        borderDash: [5, 5],
        borderWidth: 1,
        pointBackgroundColor: '#ff07de',
        pointRadius: 4,
        pointHoverRadius: 7,
      },
    ],
  };

  protected lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    interaction: { mode: 'index', intersect: false },
    color: '#ffffff',
    scales: {
      x: {
        type: 'time',
        time: { unit: 'day', tooltipFormat: 'MMM dd, yyyy' },
        grid: { color: 'rgba(255,255,255,0.06)' },
        ticks: { color: '#a497c6' },
      },
      y: {
        grid: { color: 'rgba(255,255,255,0.06)' },
        ticks: {
          color: '#a497c6',
          callback: (v) => '$' + v,
        },
      },
    },
    plugins: {
      legend: { labels: { color: '#ffffff' } },
      tooltip: {
        backgroundColor: '#1c1139',
        borderColor: 'rgba(0, 214, 160, 0.3)',
        borderWidth: 1,
        titleColor: '#ffffff',
        bodyColor: '#a497c6',
        padding: 10,
      },
      annotation: { annotations: {} },
    },
  };

  constructor() {
    effect(() => {
      const id = this.gameId();
      if (!id) return;
      this.loadAnalytics(id);
      this.loadExternalData(id);
      this.loadReviews(id);
    });
  }

  // ── Data loading ──────────────────────────────────────────────────────
  private loadAnalytics(id: string): void {
    this.gameService.getGameAnalytics(id).subscribe({
      next: (data) => {
        this.applyLocalData(data);
        this.isChartLoaded.set(true);
        const ext = this.externalData();
        if (ext) this.applyAllTimeLowAnnotation(ext);
      },
    });
  }

  private loadExternalData(id: string): void {
    this.gameService.getExternalData(id).subscribe({
      next: (data) => {
        this.externalData.set(data);
        this.isExternalLoaded.set(true);
        if (this.isChartLoaded()) this.applyAllTimeLowAnnotation(data);
      },
      error: () => this.isExternalLoaded.set(true),
    });
  }

  private loadReviews(id: string): void {
    this.reviewService.getReviewsForGame(id).subscribe({
      next: (reviews: Review[]) => {
        this.processReviewDistribution(reviews);
        this.isReviewChartLoaded.set(true);
      },
      error: (err) => {
        console.error('Failed to load reviews for chart', err);
        this.isReviewChartLoaded.set(true); // Stop the loading spinner even if it fails
      },
    });
  }

  private processReviewDistribution(reviews: Review[]): void {
    const counts = [0, 0, 0, 0, 0];

    reviews.forEach((review) => {
      // Assuming review.rating is a number from 1 to 5
      const ratingIndex = Math.floor(review.score) - 1;

      if (ratingIndex >= 0 && ratingIndex <= 4) {
        counts[ratingIndex]++;
      }
    });

    this.barChartData.datasets[0].data = counts;

    // Create a new object reference so Angular detects the change and triggers Chart.js to re-render
    this.barChartData = { ...this.barChartData };
  }

  // ── Chart updates ─────────────────────────────────────────────────────
  // ============================================================
  // Replace these two methods in game-analytics.component.ts
  // (everything else in the file stays the same)
  // ============================================================

  private applyLocalData(data: GameAnalytics): void {
    // 1. Historical prices
    this.lineChartData.datasets[0].data = data.priceHistory.map((p) => ({
      x: p.date as unknown as number,
      y: p.price,
    })) as ChartConfiguration<'line'>['data']['datasets'][number]['data'];

    // 2. Prediction line
    this.lineChartData.datasets[1].data = (data.pricePrediction ?? []).map((p) => ({
      x: p.date as unknown as number,
      y: p.price,
    })) as ChartConfiguration<'line'>['data']['datasets'][number]['data'];

    // 3. Build patch annotations, preserving any allTimeLow already added
    const patchAnnotations: Record<string, unknown> = {};
    data.patchHistory.forEach((patch, i) => {
      patchAnnotations[`patch${i}`] = {
        type: 'line',
        xMin: patch.date,
        xMax: patch.date,
        borderColor: '#ffc107',
        borderWidth: 2,
        borderDash: [5, 5],
        label: {
          display: true,
          content: patch.version,
          position: 'start',
          backgroundColor: '#ffc107',
          color: '#000',
          font: { weight: 'bold', size: 11 },
          padding: { x: 6, y: 3 },
          borderRadius: 4,
        },
      };
    });

    const existing = ((this.lineChartOptions.plugins?.annotation as any)?.annotations ??
      {}) as Record<string, unknown>;
    const allTimeLowExisting = existing['allTimeLow'];

    // New reference for both data and options → ng2-charts picks up the change
    this.lineChartData = { ...this.lineChartData };
    this.lineChartOptions = {
      ...this.lineChartOptions,
      plugins: {
        ...this.lineChartOptions.plugins,
        annotation: {
          annotations: {
            ...patchAnnotations,
            ...(allTimeLowExisting ? { allTimeLow: allTimeLowExisting } : {}),
          },
        },
      },
    };

    this.refreshChart();
  }

  private applyAllTimeLowAnnotation(data: ExternalGameData): void {
    if (!data.cheapestPriceEver) return;
    const allTimeLow = parseFloat(data.cheapestPriceEver);
    if (!Number.isFinite(allTimeLow)) return;

    const dateLabel = data.cheapestPriceDateEpoch
      ? new Date(data.cheapestPriceDateEpoch * 1000).toLocaleDateString('en-US', {
          month: 'short',
          year: 'numeric',
        })
      : '';

    // Y-min: lower of (local min, all-time low) minus a couple bucks of padding
    const localPrices = (this.lineChartData.datasets[0].data as { y: number }[]).map((p) => p.y);
    const minLocal = localPrices.length ? Math.min(...localPrices) : allTimeLow;
    const yMin = Math.max(0, Math.min(minLocal, allTimeLow) - 2);

    const allTimeLowAnnotation = {
      type: 'line' as const,
      yMin: allTimeLow,
      yMax: allTimeLow,
      borderColor: '#ff5252',
      borderWidth: 2,
      borderDash: [6, 4],
      adjustScaleRange: true, // belt-and-suspenders: also asks the plugin to extend the scale
      label: {
        display: true,
        content: dateLabel
          ? `All-time low: $${allTimeLow.toFixed(2)} · ${dateLabel}`
          : `All-time low: $${allTimeLow.toFixed(2)}`,
        position: 'end' as const,
        backgroundColor: 'rgba(255, 82, 82, 0.95)',
        color: '#fff',
        font: { size: 11, weight: 'bold' as const },
        padding: { x: 8, y: 4 },
        borderRadius: 4,
        yAdjust: -12,
      },
    };

    // Preserve patch annotations
    const currentAnnotations = ((this.lineChartOptions.plugins?.annotation as any)?.annotations ??
      {}) as Record<string, unknown>;

    // New reference all the way down → ng2-charts ngOnChanges fires
    this.lineChartOptions = {
      ...this.lineChartOptions,
      scales: {
        ...this.lineChartOptions.scales,
        y: {
          ...(this.lineChartOptions.scales!['y'] as any),
          min: yMin,
        },
      },
      plugins: {
        ...this.lineChartOptions.plugins,
        annotation: {
          annotations: {
            ...currentAnnotations,
            allTimeLow: allTimeLowAnnotation,
          },
        },
      },
    };

    this.refreshChart();
  }
  private refreshChart(): void {
    queueMicrotask(() => this.chart?.update());
  }
}
