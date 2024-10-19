import { Component } from '@angular/core';
import { TranslateDirective } from 'app/shared/language';
import { LayoutService } from '../service/layout.service';

@Component({
  standalone: true,
  selector: 'jhi-footer',
  templateUrl: './footer.component.html',
  imports: [TranslateDirective],
})
export default class FooterComponent {
  constructor(public layoutService: LayoutService) {}
}
