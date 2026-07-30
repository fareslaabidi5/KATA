import { Component } from '@angular/core';
import { Dashboard } from './dashboard/dashboard';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [Dashboard],
  template: '<app-dashboard></app-dashboard>'
})
export class App {
}
