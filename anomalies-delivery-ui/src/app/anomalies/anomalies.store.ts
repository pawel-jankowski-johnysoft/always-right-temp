import {patchState, signalStore, withMethods, withState} from '@ngrx/signals';

export type Anomaly = {
  roomId: number, thermometerId: number, temperature: number, timestamp: number
}
export type AnomaliesState = {
  anomalies: Anomaly[], showSnacks: boolean, streamEnabled: boolean
}

const initState: AnomaliesState = {
  anomalies: [], showSnacks: false, streamEnabled: false
}

export const AnomaliesStore = signalStore({providedIn: 'root'},
  withState(initState),
  withMethods(store => ({
    async addAnomaly(anomaly: any) {
      patchState(store, {anomalies: [...store.anomalies(), anomaly]})
    },

    async enableStream() {
      patchState(store, {streamEnabled: true, anomalies: []})
    },

    async disableStream() {
      patchState(store, {streamEnabled: false})
    }
  }))
)
