import {AfterViewInit, Component, effect, inject, ViewChild} from '@angular/core';
import {AnomaliesStore, Anomaly} from "./anomalies.store";
import {getState} from "@ngrx/signals";
import {AnomaliesMenuComponent} from "./anomalies-menu/anomalies-menu.component";
import {MatTableDataSource, MatTableModule} from "@angular/material/table";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {DatePipe, NgIf} from "@angular/common";
import {AnomaliesStatisticsComponent} from "./anomalies-statistics/anomalies-statistics.component";

@Component({
  selector: 'anomalies',
  standalone: true,
  imports: [AnomaliesMenuComponent, MatTableModule, MatPaginatorModule, DatePipe, AnomaliesStatisticsComponent, NgIf ],
  templateUrl: './anomalies.component.html',
  styleUrl: './anomalies.component.scss'
})
export class AnomaliesComponent implements AfterViewInit {
  readonly store = inject(AnomaliesStore);
  displayedColumns = ['room', 'thermometer', 'temperature', 'timestamp'];

  dataSource = new MatTableDataSource<Anomaly>([]);

  // @ts-ignore
  @ViewChild(MatPaginator) paginator: MatPaginator;

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  constructor() {
    effect(() => {
      const state = getState(this.store)
      this.dataSource.data = state.anomalies;
    });
  }
}

