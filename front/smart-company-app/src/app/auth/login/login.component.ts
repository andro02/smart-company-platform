import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AxiosService } from '../axios.service';
import { Credentials } from '../model/credentials';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule, ReactiveFormsModule]
})
export class LoginComponent {

  loginForm: FormGroup;
  errorMessage: string = '';
  // while the login request is on its way (it can take a few seconds on the
  // demo server): shows a spinner and stops the form being sent twice
  loggingIn = false;

  constructor(private formBuilder: FormBuilder, private axiosService: AxiosService, private router: Router) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    if (this.loggingIn) return;
    this.errorMessage = '';
    this.loggingIn = true;

    const loginData = this.loginForm.value;
    const credentials = new Credentials(loginData.username, loginData.password);

    this.axiosService.request('POST', '/login', credentials)
      .then(response => {
        this.axiosService.setAuthToken(response.data.token);
        const mustChangePassword = this.axiosService.getMustChangePassword();

        if (mustChangePassword) {
          this.router.navigate(['change-password']);
          return;
        }

        const role = this.axiosService.getRole();
        switch (role) {
          case 'SuperAdmin':
          case 'Manager':
            this.router.navigate(['products']);
            break;
          default:
            this.router.navigate(['products']);
        }
      })
      .catch(error => {
        this.axiosService.setAuthToken(null);
        this.errorMessage = error?.response?.data?.message || 'Pogrešno korisničko ime ili lozinka.';
      })
      .finally(() => {
        this.loggingIn = false;
      });
  }
}