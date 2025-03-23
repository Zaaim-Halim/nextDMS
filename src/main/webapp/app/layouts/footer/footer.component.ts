import { Component, OnInit } from '@angular/core';
import { LayoutService } from '../service/layout.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
})
export default class FooterComponent implements OnInit {
  currentYear!: number;
  constructor(public layoutService: LayoutService) {}
  ngOnInit(): void {
    this.currentYear = new Date().getFullYear();
  }
}
