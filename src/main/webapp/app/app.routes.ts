import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'home.title',
    data: { showTopBar: true, showSideBar: true, showFooter: true },
  },
  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
      showTopBar: true,
      showSideBar: true,
      showFooter: true,
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
    data: { showTopBar: false, showSideBar: false, showFooter: true },
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component'),
    title: 'login.title',
    data: { showTopBar: false, showSideBar: false, showFooter: true },
  },
  {
    path: '',
    loadChildren: () => import(`./entities/entity.routes`),
    data: { showTopBar: true, showSideBar: true, showFooter: true },
  },
  ...errorRoute,
];

export default routes;
