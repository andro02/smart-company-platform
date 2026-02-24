import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { RegisterManagerComponent } from './superadmin/register-manager/register-manager.component';
import { HomeComponent } from './home/home.component';
import { ActivationComponent } from './auth/activation/activation.component';

export const routes: Routes = [
    { component: HomeComponent, path: "" },
    { component: LoginComponent, path: "login" },
    { component: RegisterComponent, path: "register" },
    { component: RegisterManagerComponent, path: "register-manager" },
    { component: ActivationComponent, path: "activate" },
];
