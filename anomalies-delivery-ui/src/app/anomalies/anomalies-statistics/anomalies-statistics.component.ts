import {Component} from '@angular/core';
import {NgxChartsModule} from "@swimlane/ngx-charts";
import {Observable} from "rxjs";
import {StatisticsProviderService} from "./statistics.provider.service";

@Component({
  selector: 'anomalies-statistics',
  standalone: true,
  imports: [
    NgxChartsModule
  ],
  templateUrl: './anomalies-statistics.component.html',
  styleUrl: './anomalies-statistics.component.scss'
})
export class AnomaliesStatisticsComponent {

  // @ts-ignore
  statistics: Observable<any[]>;

  constructor(private readonly statisticsProvider: StatisticsProviderService) {
    this.statistics = this.statisticsProvider.getStatistics();
  }

  colorScheme: any = {
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };

}
