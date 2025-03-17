import { Pipe, PipeTransform } from '@angular/core';

import dayjs from 'dayjs/esm';
import { TranslateService } from '@ngx-translate/core';

@Pipe({
  standalone: true,
  name: 'formatTimeAgo',
})
export default class formatTimeAgoPipe implements PipeTransform {
  constructor(private translate: TranslateService) {}
  transform(inputDate: dayjs.Dayjs | null | undefined): string {
    if (!inputDate) {
      return '';
    } else {
      const now = dayjs();
      const diffInSeconds = now.diff(inputDate, 'second');
      const diffInMinutes = now.diff(inputDate, 'minute');
      const diffInHours = now.diff(inputDate, 'hour');
      const diffInDays = now.diff(inputDate, 'day');
      const diffInWeeks = Math.floor(diffInDays / 7); // Calculate weeks
      const diffInMonths = now.diff(inputDate, 'month');
      const diffInYears = now.diff(inputDate, 'year');

      // Return the appropriate time period
      if (diffInYears > 0) {
        return this.translate.instant('timeAgo.year', { years: diffInYears });
      }
      if (diffInMonths > 0) {
        return this.translate.instant('timeAgo.month', { months: diffInMonths });
      }
      if (diffInWeeks > 0) {
        return this.translate.instant('timeAgo.week', { weeks: diffInWeeks });
      }
      if (diffInDays > 0) {
        return this.translate.instant('timeAgo.day', { days: diffInDays });
      }
      if (diffInHours > 0) {
        return this.translate.instant('timeAgo.hour', { hours: diffInHours });
      }
      if (diffInMinutes > 0) {
        return this.translate.instant('timeAgo.minute', { minutes: diffInMinutes });
      }
      return this.translate.instant('timeAgo.second', { seconds: diffInSeconds });
    }
  }
}
