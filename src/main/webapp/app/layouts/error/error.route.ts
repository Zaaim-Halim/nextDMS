import { Routes } from '@angular/router';

export const errorRoute: Routes = [
  {
    path: 'error',
    loadComponent: () => import('./error.component'),
    title: 'error.title',
    data: { showTopBar: false, showSideBar: false, showFooter: true },
  },
  {
    path: 'accessdenied',
    loadComponent: () => import('./error.component'),
    data: {
      errorMessage: 'error.http.403',
      showTopBar: false,
      showSideBar: false,
      showFooter: true,
    },
    title: 'error.title',
  },
  {
    path: '404',
    loadComponent: () => import('./error.component'),
    data: {
      errorMessage: 'error.http.404',
      showTopBar: false,
      showSideBar: false,
      showFooter: true,
    },
    title: 'error.title',
  },
  {
    path: '**',
    redirectTo: '/404',
    data: { showTopBar: false, showSideBar: false, showFooter: true },
  },
];
