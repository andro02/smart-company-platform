import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { RegisterManagerComponent } from './superadmin/register-manager/register-manager.component';
import { HomeComponent } from './home/home.component';
import { ActivationComponent } from './auth/activation/activation.component';
import { ProductListComponent } from './product/product-list/product-list.component';
import { ProductFormComponent } from './product/product-form/product-form.component';
import { ChangePasswordComponent } from './auth/change-password/change-password.component';

export const routes: Routes = [
  { path: '',                   component: HomeComponent },
  { path: 'login',              component: LoginComponent },
  { path: 'register',           component: RegisterComponent },
  { path: 'register-manager',   component: RegisterManagerComponent },
  { path: 'activate',           component: ActivationComponent },
  { path: 'products',           component: ProductListComponent },
  { path: 'products/new',       component: ProductFormComponent },
  { path: 'products/edit/:id',  component: ProductFormComponent },
  { path: 'change-password',    component: ChangePasswordComponent },
];