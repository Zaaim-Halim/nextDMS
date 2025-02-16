import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
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
import { RouterModule } from '@angular/router';
import { AppConfigModule } from '../config/config.module';
import { AppSidebarComponent } from '../sidebar/sidebar.component';
import FooterComponent from '../footer/footer.component';

@NgModule({
  declarations: [AppLayoutComponent],
  imports: [
    BrowserModule,
    FormsModule,
    BrowserAnimationsModule,
    InputTextModule,
    SidebarModule,
    BadgeModule,
    RadioButtonModule,
    InputSwitchModule,
    RippleModule,
    RouterModule,
    AppConfigModule,
    AppMenuitemComponent,
    AppTopBarComponent,
    AppMenuComponent,
    AppSidebarComponent,
    FooterComponent,
  ],
  exports: [AppLayoutComponent],
})
export class AppLayoutModule {}
