import { Component, Input, OnInit, inject } from '@angular/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartOptions, Chart, registerables } from 'chart.js';
import annotationPlugin from 'chartjs-plugin-annotation';
import { VideoGameService } from '../../services/video-game.service'; // Pune calea corectă către serviciul tău
import { GameAnalytics } from '../../models/game-analytics'; // Pune calea corectă către interfețele de mai sus
import 'chartjs-adapter-date-fns';

Chart.register(...registerables, annotationPlugin);

@Component({
  selector: 'app-game-analytics',
  standalone: true,
  imports: [BaseChartDirective],
  templateUrl: './game-analytics.component.html',
  styleUrls: ['./game-analytics.component.scss'],
})
export class GameAnalyticsComponent implements OnInit {
  @Input({ required: true }) gameId!: string | undefined; // Primim ID-ul jocului din exterior
  private gameService = inject(VideoGameService); // Injectăm serviciul

  public isDataLoaded = false; // Folosim asta ca să nu randăm graficul gol până vin datele

  // Setările graficului (fără date încă)
  public lineChartData: ChartConfiguration<'line'>['data'] = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Preț ($)',
        fill: true,
        tension: 0.3,
        borderColor: '#00d6a0',
        backgroundColor: 'rgba(0, 214, 160, 0.1)',
        pointBackgroundColor: '#ffffff',
      },
    ],
  };

  // Setările de design (fără adnotări statice)
  public lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    color: '#ffffff',
    scales: {
      x: {
        type: 'time', // ASTA E MAGIA!
        time: {
          unit: 'day',
          tooltipFormat: 'MMM dd, yyyy', // Cum arată data când pui mouse-ul pe ea
        },
        grid: { color: 'rgba(255, 255, 255, 0.1)' },
      },
      y: { grid: { color: 'rgba(255, 255, 255, 0.1)' } },
    },
    plugins: {
      legend: { labels: { color: 'white' } },
      annotation: { annotations: {} }, // Va fi populat dinamic
    },
  };

  ngOnInit() {
    this.gameService.getGameAnalytics(this.gameId).subscribe({
      next: (data: GameAnalytics) => {
        this.processChartData(data);
        this.isDataLoaded = true; // Afișăm graficul doar după ce am terminat procesarea
      },
      error: (err) => console.error('Eroare la încărcarea datelor de analytics', err),
    });
  }

  private processChartData(data: GameAnalytics) {
    // 1. Populăm Axa X (datele) și Axa Y (prețurile)
    this.lineChartData.labels = [];

    this.lineChartData.datasets[0].data = data.priceHistory.map((p) => ({
      x: p.date,
      y: p.price,
    })) as any;

    // 2. Creăm dinamic adnotările (liniile verticale pentru patch-uri)
    const dynamicAnnotations: any = {};

    data.patchHistory.forEach((patch, index) => {
      dynamicAnnotations[`patch${index}`] = {
        type: 'line',
        xMin: patch.date, // Data unde tragem linia
        xMax: patch.date,
        borderColor: '#ffc107',
        borderWidth: 2,
        borderDash: [5, 5],
        label: {
          display: true,
          content: patch.version, // Numele versiunii ex: "v1.2"
          position: 'start',
          backgroundColor: '#ffc107',
          color: 'black',
          font: { weight: 'bold' },
        },
      };
    });

    // Punem adnotările în setările graficului
    this.lineChartOptions.plugins!.annotation!.annotations = dynamicAnnotations;
  }
}
