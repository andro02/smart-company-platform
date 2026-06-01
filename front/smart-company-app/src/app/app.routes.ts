import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { RegisterManagerComponent } from './superadmin/register-manager/register-manager.component';
import { ManagerListComponent } from './superadmin/manager-list/manager-list.component';
import { HomeComponent } from './home/home.component';
import { ActivationComponent } from './auth/activation/activation.component';
import { ProductListComponent } from './product/product-list/product-list.component';
import { ProductFormComponent } from './product/product-form/product-form.component';
import { ChangePasswordComponent } from './auth/change-password/change-password.component';
import { FactoryListComponent } from './factory/factory-list/factory-list.component';
import { FactoryFormComponent } from './factory/factory-form/factory-form.component';
import { FactoryDetailComponent } from './factory/factory-detail/factory-detail.component';
import { authGuard, mustChangePasswordGuard } from './common/guards/auth.guard';

export const routes: Routes = [
  { path: '',                   component: HomeComponent },

  // Javne rute
  { path: 'login',              component: LoginComponent },
  { path: 'register',           component: RegisterComponent },
  { path: 'activate',           component: ActivationComponent },

  { path: 'change-password',    component: ChangePasswordComponent, canActivate: [mustChangePasswordGuard] },

  // Proizvodi
  { path: 'products',           component: ProductListComponent,     canActivate: [authGuard] },
  { path: 'products/new',       component: ProductFormComponent,     canActivate: [authGuard] },
  { path: 'products/edit/:id',  component: ProductFormComponent,     canActivate: [authGuard] },

  // Menadžeri
  { path: 'register-manager',   component: RegisterManagerComponent, canActivate: [authGuard] },
  { path: 'managers',           component: ManagerListComponent,     canActivate: [authGuard] },

  // Fabrike
  { path: 'factories',          component: FactoryListComponent,     canActivate: [authGuard] },
  { path: 'factories/new',      component: FactoryFormComponent,     canActivate: [authGuard] },
  { path: 'factories/edit/:id', component: FactoryFormComponent,     canActivate: [authGuard] },
  { path: 'factories/:id',      component: FactoryDetailComponent,   canActivate: [authGuard] },

  { path: '**',                 redirectTo: '' }
];