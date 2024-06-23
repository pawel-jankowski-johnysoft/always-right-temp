import {Routes} from '@angular/router';

export const routes: Routes = [
  {

    path: '',
    children: [
      {
        path: 'anomalies',
        loadComponent: () => import('./anomalies/anomalies.component').then(anomaliesModule => anomaliesModule.AnomaliesComponent)
      }
    ]
  }
];
