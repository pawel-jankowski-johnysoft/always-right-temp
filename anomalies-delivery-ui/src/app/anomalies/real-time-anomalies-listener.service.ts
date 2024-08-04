import {inject, Injectable} from '@angular/core';
import {SseClient} from "angular-sse-client";
import {Subscription} from "rxjs";
import {AnomaliesStore} from "./anomalies.store";

@Injectable({
  providedIn: 'root'
})
export class RealTimeAnomaliesListener {

  private static readonly ANOMALIES_DELIVERY_URL = '/api/anomalies/sse';

  private observers: AnomalyListener[] = [new PrintAnomalyToConsoleListener()]

  private subscription: Subscription | null = null;
  private store = inject(AnomaliesStore)


  constructor(private readonly sseClient: SseClient) { }

  registerListener(listener: AnomalyListener) {
    this.observers.push(listener)
  }

  removeListener(listener: AnomalyListener) {
    this.observers = this.observers.filter(it => it != listener)
  }

  public enable(): void {
    this.store.enableStream()
      .then(_ => {
        this.subscription = this.sseClient.get(RealTimeAnomaliesListener.ANOMALIES_DELIVERY_URL)
          .subscribe(anomaly => {
            this.store.addAnomaly(anomaly)
            this.observers.forEach(observer => observer.accept(anomaly))
          });
    })
  }

  public disable(): void {
    this.store.disableStream()
      .then(_ => {
        this.subscription?.unsubscribe();
        this.subscription = null;
      })
  }

  isEnabled() {
    return this.store.streamEnabled();
  }

  isShowSnacksEnabled() {
    return this.store.showSnacks();
  }

}
export interface AnomalyListener {
  accept(anomaly: any): void;
}

class PrintAnomalyToConsoleListener implements AnomalyListener {
    accept(anomaly: any): void {
      console.error(anomaly);
    }
}
