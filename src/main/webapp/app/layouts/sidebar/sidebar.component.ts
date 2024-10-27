import { Component, ElementRef } from '@angular/core';
import { LayoutService } from '../service/layout.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  templateUrl: './app.sidebar.component.html',
})
export class AppSidebarComponent {
  constructor(
    public layoutService: LayoutService,
    public el: ElementRef,
  ) {}
}
