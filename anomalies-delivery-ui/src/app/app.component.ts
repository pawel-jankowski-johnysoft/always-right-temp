import {Component} from '@angular/core';
import {RouterModule} from '@angular/router';
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatListModule} from "@angular/material/list";

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [MatSidenavModule, MatListModule, RouterModule],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
}


