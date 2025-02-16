import { Component, ElementRef } from '@angular/core';
import { LayoutService } from '../service/layout.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
})
export class AppSidebarComponent {
  constructor(
    public layoutService: LayoutService,
    public el: ElementRef,
  ) {}
}
