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
import { authGuard, mustChangePasswordGuard } from './common/guards/auth.guard';

export const routes: Routes = [
  // Home — redirect na products ili login u zavisnosti od toga da li je ulogovan
  { path: '',                   component: HomeComponent },

  // Javne rute
  { path: 'login',              component: LoginComponent },
  { path: 'register',           component: RegisterComponent },
  { path: 'activate',           component: ActivationComponent },

  // Promena lozinke — dostupno samo ako mustChangePassword=true
  { path: 'change-password',    component: ChangePasswordComponent, canActivate: [mustChangePasswordGuard] },

  // Zaštićene rute
  { path: 'products',           component: ProductListComponent,     canActivate: [authGuard] },
  { path: 'products/new',       component: ProductFormComponent,     canActivate: [authGuard] },
  { path: 'products/edit/:id',  component: ProductFormComponent,     canActivate: [authGuard] },
  { path: 'register-manager',   component: RegisterManagerComponent, canActivate: [authGuard] },
  { path: 'managers',           component: ManagerListComponent,     canActivate: [authGuard] },

  // Sve ostalo — redirect na home
  { path: '**',                 redirectTo: '' }
];