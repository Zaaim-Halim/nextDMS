import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { SidebarModule } from 'primeng/sidebar';
import { BadgeModule } from 'primeng/badge';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputSwitchModule } from 'primeng/inputswitch';
import { RippleModule } from 'primeng/ripple';
import { AppLayoutComponent } from './app.layout.component';
import { AppMenuitemComponent } from '../menuitem/menuitem.component';
import { AppTopBarComponent } from '../topbar/topbar.component';
import { AppMenuComponent } from '../menu/menu.component';
import { RouterModule, RouterOutlet } from '@angular/router';
import { AppConfigModule } from '../config/config.module';
import { AppSidebarComponent } from '../sidebar/sidebar.component';
import FooterComponent from '../footer/footer.component';
import { CommonModule } from '@angular/common';

@NgModule({
  declarations: [AppLayoutComponent, AppSidebarComponent, AppMenuitemComponent, AppTopBarComponent, AppMenuComponent, FooterComponent],
  imports: [
    FormsModule,
    CommonModule,
    InputTextModule,
    SidebarModule,
    BadgeModule,
    RadioButtonModule,
    InputSwitchModule,
    RippleModule,
    AppConfigModule,
    RouterOutlet,
    RouterModule,
  ],
  exports: [AppLayoutComponent, FooterComponent],
})
export class AppLayoutModule {}
