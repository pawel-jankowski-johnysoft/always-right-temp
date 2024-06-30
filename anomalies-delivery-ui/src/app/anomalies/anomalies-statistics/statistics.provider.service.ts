import {effect, EventEmitter, Inject, Injectable} from '@angular/core';
import {AnomaliesStore, Anomaly} from "../anomalies.store";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class StatisticsProviderService {

  private statistics = new EventEmitter<any[]>();
  constructor(@Inject(AnomaliesStore) store: any) {
    effect(() => this.statistics.emit(this.groupByRoom(store.anomalies())));
  }

  private groupByRoom(anomalies: Anomaly[]) {
    const result: any[] = []
    const rooms = new Set(anomalies.map(it => it.roomId));
    rooms.forEach(roomId => result.push({name: `room: ${roomId}`, value: anomalies.filter(it => it.roomId === roomId).length}));
    return result;
  }

  getStatistics(): Observable<any[]> {
    return this.statistics.asObservable();
  }
}
