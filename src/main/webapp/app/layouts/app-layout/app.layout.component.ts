import { Component, inject, OnDestroy, OnInit, Renderer2, ViewChild } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AppSidebarComponent } from '../sidebar/sidebar.component';
import { AppTopBarComponent } from '../topbar/topbar.component';
import { LayoutService } from '../service/layout.service';
import { filter } from 'rxjs/operators';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import dayjs from 'dayjs/esm';
import { AccountService } from '../../core/auth/account.service';
import { AppPageTitleStrategy } from '../../app-page-title-strategy';

@Component({
  selector: 'app-layout',
  templateUrl: './app.layout.component.html',
})
export class AppLayoutComponent implements OnInit, OnDestroy {
  overlayMenuOpenSubscription: Subscription;
  accountService = inject(AccountService);
  translateService = inject(TranslateService);
  appPageTitleStrategy = inject(AppPageTitleStrategy);
  profileMenuOutsideClickListener: any;
  menuOutsideClickListener: any;

  showTopBar = false;
  showSideBar = false;
  showFooter = false;

  @ViewChild(AppSidebarComponent) appSidebar!: AppSidebarComponent;

  @ViewChild(AppTopBarComponent) appTopbar!: AppTopBarComponent;

  constructor(
    public layoutService: LayoutService,
    public renderer: Renderer2,
    public router: Router,
    private activatedRoute: ActivatedRoute,
  ) {
    this.overlayMenuOpenSubscription = this.layoutService.overlayOpen$.subscribe(() => {
      if (!this.menuOutsideClickListener) {
        this.menuOutsideClickListener = this.renderer.listen('document', 'click', event => {
          const isOutsideClicked = !(
            this.appSidebar.el.nativeElement.isSameNode(event.target) ||
            this.appSidebar.el.nativeElement.contains(event.target) ||
            this.appTopbar.menuButton.nativeElement.isSameNode(event.target) ||
            this.appTopbar.menuButton.nativeElement.contains(event.target)
          );

          if (isOutsideClicked) {
            this.hideMenu();
          }
        });
      }

      if (!this.profileMenuOutsideClickListener) {
        this.profileMenuOutsideClickListener = this.renderer.listen('document', 'click', event => {
          const isOutsideClicked = !(
            this.appTopbar.menu.nativeElement.isSameNode(event.target) ||
            this.appTopbar.menu.nativeElement.contains(event.target) ||
            this.appTopbar.topbarMenuButton.nativeElement.isSameNode(event.target) ||
            this.appTopbar.topbarMenuButton.nativeElement.contains(event.target)
          );

          if (isOutsideClicked) {
            this.hideProfileMenu();
          }
        });
      }

      if (this.layoutService.state.staticMenuMobileActive) {
        this.blockBodyScroll();
      }
    });

    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      this.hideMenu();
      this.hideProfileMenu();
    });
  }
  ngOnInit(): void {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.showTopBar = this.activatedRoute.firstChild?.snapshot.data.showTopBar !== false;
        this.showSideBar = this.activatedRoute.firstChild?.snapshot.data.showSideBar !== false;
        this.showFooter = this.activatedRoute.firstChild?.snapshot.data.showFooter !== false;
      }
    });
    this.accountService.identity().subscribe();
    this.translateService.onLangChange.subscribe((langChangeEvent: LangChangeEvent) => {
      this.appPageTitleStrategy.updateTitle(this.router.routerState.snapshot);
      dayjs.locale(langChangeEvent.lang);
      this.renderer.setAttribute(document.querySelector('html'), 'lang', langChangeEvent.lang);
    });
  }
  hideMenu(): void {
    this.layoutService.state.overlayMenuActive = false;
    this.layoutService.state.staticMenuMobileActive = false;
    this.layoutService.state.menuHoverActive = false;
    if (this.menuOutsideClickListener) {
      this.menuOutsideClickListener();
      this.menuOutsideClickListener = null;
    }
    this.unblockBodyScroll();
  }

  hideProfileMenu(): void {
    this.layoutService.state.profileSidebarVisible = false;
    if (this.profileMenuOutsideClickListener) {
      this.profileMenuOutsideClickListener();
      this.profileMenuOutsideClickListener = null;
    }
  }

  blockBodyScroll(): void {
    document.body.className += ' blocked-scroll';
    /*if (document.body.classList){
            document.body.classList.add('blocked-scroll');
        }else {
            document.body.className += ' blocked-scroll';
        }*/
  }

  unblockBodyScroll(): void {
    document.body.className = document.body.className.replace(
      new RegExp('(^|\\b)' + 'blocked-scroll'.split(' ').join('|') + '(\\b|$)', 'gi'),
      ' ',
    );
    /*if (document.body.classList) {
            document.body.classList.remove('blocked-scroll');
        }
        else {
            document.body.className = document.body.className.replace(new RegExp('(^|\\b)' +
                'blocked-scroll'.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
        }*/
  }

  get containerClass(): any {
    return {
      'layout-theme-light': this.layoutService.config().colorScheme === 'light',
      'layout-theme-dark': this.layoutService.config().colorScheme === 'dark',
      'layout-overlay': this.layoutService.config().menuMode === 'overlay',
      'layout-static': this.layoutService.config().menuMode === 'static',
      'layout-static-inactive': this.layoutService.state.staticMenuDesktopInactive && this.layoutService.config().menuMode === 'static',
      'layout-overlay-active': this.layoutService.state.overlayMenuActive,
      'layout-mobile-active': this.layoutService.state.staticMenuMobileActive,
      'p-input-filled': this.layoutService.config().inputStyle === 'filled',
      'p-ripple-disabled': !this.layoutService.config().ripple,
    };
  }

  ngOnDestroy(): void {
    this.overlayMenuOpenSubscription.unsubscribe();

    if (this.menuOutsideClickListener) {
      this.menuOutsideClickListener();
    }
  }
}
