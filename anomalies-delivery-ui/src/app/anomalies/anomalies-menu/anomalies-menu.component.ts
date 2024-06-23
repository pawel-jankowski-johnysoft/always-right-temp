import {Component, Inject, inject, Injectable} from '@angular/core';
import {MatButtonModule} from "@angular/material/button";
import {AnomalyListener, RealTimeAnomaliesListener} from "../real-time-anomalies-listener.service";
import {MAT_SNACK_BAR_DATA, MatSnackBar} from "@angular/material/snack-bar";
import {noop} from "rxjs";
import {AnomaliesStore} from "../anomalies.store";
import {patchState} from "@ngrx/signals";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatMenuModule} from "@angular/material/menu";
import {MatIconModule} from "@angular/material/icon";
import {JsonPipe, NgIf} from "@angular/common";


@Component({
  selector: 'anomaly-detected-alert',
  imports: [JsonPipe],
  template: '<div>detected anomaly {{data | json }}</div>',
  styles: `
      .example-pizza-party {
        color: red;
      }
    `,
  standalone: true,
})
class AnomalyDetectedAlert {
  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) {
  }
}


@Injectable()
export class DisplayNewEvenAsSnackbarListener  implements AnomalyListener {

  constructor(private readonly snackBar: MatSnackBar) {
  }
  accept(anomaly: any): void {
    this.snackBar.openFromComponent(AnomalyDetectedAlert, {duration: 2000, data:anomaly })
      .afterOpened()
      .subscribe(noop)
  }
}

@Component({
  selector: 'anomalies-menu',
  standalone: true,
  imports: [MatButtonModule, MatToolbarModule, MatMenuModule, MatIconModule, NgIf],
  providers: [DisplayNewEvenAsSnackbarListener],
  templateUrl: './anomalies-menu.component.html',
  styleUrl: './anomalies-menu.component.scss'
})
export class AnomaliesMenuComponent {
  store = inject(AnomaliesStore)
  constructor(protected realTimeAnomaliesListener: RealTimeAnomaliesListener, private displayNewEvenAsSnackbar: DisplayNewEvenAsSnackbarListener) {
  }

  registerSnackbarListener() {
    patchState(this.store, {showSnacks: true});
    this.realTimeAnomaliesListener.registerListener(this.displayNewEvenAsSnackbar)
  }

  unregisterSnackbarListener() {
    patchState(this.store, {showSnacks: false});
    this.realTimeAnomaliesListener.removeListener(this.displayNewEvenAsSnackbar)
  }
}
