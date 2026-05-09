import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
  signal,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, Chart, registerables } from 'chart.js';
import annotationPlugin from 'chartjs-plugin-annotation';
import { VideoGameService } from '../../services/video-game.service';
import { GameAnalytics } from '../../models/game-analytics';
import { ExternalGameData } from '../../models/external-game-data.model';
import 'chartjs-adapter-date-fns';

Chart.register(...registerables, annotationPlugin);

@Component({
  selector: 'app-game-analytics',
  standalone: true,
  imports: [BaseChartDirective, CommonModule],
  templateUrl: './game-analytics.component.html',
  styleUrls: ['./game-analytics.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameAnalyticsComponent implements OnInit {
  @Input({ required: true }) gameId!: string | undefined;

  private readonly gameService = inject(VideoGameService);

  isChartLoaded = signal(false);
  externalData = signal<ExternalGameData | null>(null);
  isExternalLoaded = signal(false);

  lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [{
      data: [],
      label: 'Price ($)',
      fill: true,
      tension: 0.4,
      borderColor: '#00d6a0',
      backgroundColor: 'rgba(0, 214, 160, 0.08)',
      pointBackgroundColor: '#00d6a0',
      pointRadius: 4,
      pointHoverRadius: 6,
    }],
  };

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
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
      annotation: { annotations: {} },
    },
  };

  ngOnInit() {
    this.gameService.getGameAnalytics(this.gameId).subscribe({
      next: (data: GameAnalytics) => {
        this.applyLocalData(data);
        this.isChartLoaded.set(true);
      },
    });

    if (this.gameId) {
      this.gameService.getExternalData(this.gameId).subscribe({
        next: (data) => {
          this.externalData.set(data);
          this.isExternalLoaded.set(true);
          this.applyAllTimeLowAnnotation(data);
        },
      });
    }
  }

  private applyLocalData(data: GameAnalytics) {
    this.lineChartData.datasets[0].data = data.priceHistory.map((p) => ({
      x: p.date,
      y: p.price,
    })) as any;

    const annotations: any = {};
    data.patchHistory.forEach((patch, i) => {
      annotations[`patch${i}`] = {
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
        },
      };
    });
    (this.lineChartOptions.plugins!.annotation as any).annotations = annotations;
  }

  private applyAllTimeLowAnnotation(data: ExternalGameData) {
    if (!data.cheapestPriceEver) return;
    const existing =
      (this.lineChartOptions.plugins!.annotation as any).annotations ?? {};

    existing['allTimeLow'] = {
      type: 'line',
      yMin: parseFloat(data.cheapestPriceEver),
      yMax: parseFloat(data.cheapestPriceEver),
      borderColor: '#e53935',
      borderWidth: 1,
      borderDash: [4, 4],
      label: {
        display: true,
        content: `All-time low: $${data.cheapestPriceEver}`,
        position: 'end',
        backgroundColor: '#e53935',
        color: '#fff',
        font: { size: 11 },
      },
    };

    this.lineChartOptions = { ...this.lineChartOptions };
  }

  epochToDate(epoch: number): string {
    return new Date(epoch * 1000).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  }
}
