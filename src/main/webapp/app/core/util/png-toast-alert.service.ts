import { Injectable, SecurityContext, inject } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';

import { translationNotFoundMessage } from 'app/config/translation.config';
import { Message, MessageService } from 'primeng/api';
import { Alert } from './alert.model';

@Injectable({
  providedIn: 'root',
})
export class NgToastAlertService {
  // unique id for each alert. Starts from 0.
  private alertId = 0;

  private sanitizer = inject(DomSanitizer);
  private translateService = inject(TranslateService);
  constructor(private pNgservice: MessageService) {}

  /**
   * Adds <p-tost> primeNg alert.
   * @param alertToAdd
   */
  addAlert(alertToAdd: Omit<Alert, 'id'>): void {
    const alert: Alert = { ...alertToAdd, id: this.alertId++ };
    if (alert.translationKey) {
      const translatedMessage = this.translateService.instant(alert.translationKey, alert.translationParams);
      // if translation key exists
      if (translatedMessage !== `${translationNotFoundMessage}[${alert.translationKey}]`) {
        alert.message = translatedMessage;
      } else if (!alert.message) {
        alert.message = alert.translationKey;
      }
    }
    const summary = this.translateService.instant('alert.' + String(alert.message));
    let type = 'error';
    if (alert.type === 'danger') {
      type = 'error';
    }
    if (alert.type === 'success') {
      type = 'success';
    }
    if (alert.type === 'info') {
      type = 'info';
    }
    if (alert.type === 'warning') {
      type = 'warn';
    }
    alert.message = this.sanitizer.sanitize(SecurityContext.HTML, alert.message ?? '') ?? '';
    this.pNgservice.add({ severity: type, summary: alert.message, detail: alert.message });
  }
  addAlertPNG(alertToAdd: Message): void {
    this.pNgservice.add(alertToAdd);
  }
}
