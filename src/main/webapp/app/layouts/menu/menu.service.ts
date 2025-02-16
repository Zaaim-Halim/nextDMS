import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { MenuChangeEvent } from '../api/menuchangeevent';

@Injectable({
  providedIn: 'root',
})
export class MenuService {
  menuSource = new Subject<MenuChangeEvent>();
  resetSource = new Subject();

  menuSource$ = this.menuSource.asObservable();
  resetSource$ = this.resetSource.asObservable();

  onMenuStateChange(event: MenuChangeEvent): void {
    this.menuSource.next(event);
  }

  reset(): void {
    this.resetSource.next(true);
  }
}
