import { Component, ElementRef } from '@angular/core';
import { LayoutService } from '../service/layout.service';
import { AppMenuComponent } from '../menu/menu.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  templateUrl: './sidebar.component.html',
  imports: [AppMenuComponent],
})
export class AppSidebarComponent {
  constructor(
    public layoutService: LayoutService,
    public el: ElementRef,
  ) {}
}
