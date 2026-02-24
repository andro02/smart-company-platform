import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AxiosService } from '../axios.service';
import { Credentials } from '../model/credentials';
import { error } from 'console';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule, ReactiveFormsModule]
})
export class LoginComponent {

  loginForm: FormGroup;

  constructor(private formBuilder: FormBuilder, private axiosService: AxiosService, private router: Router) {

    this.loginForm = this.formBuilder.group({
      username: ["", Validators.compose([Validators.required, Validators.minLength(5)])],
      password: ["", Validators.compose([Validators.required, Validators.minLength(5)])]
    });

  }

  onSubmit(): void {

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const loginData = this.loginForm.value;
    const credentials = new Credentials(
      loginData.username,
      loginData.password
    );

    this.axiosService.request(
      "POST",
      "/login",
      credentials
    ).then(response => {
      this.axiosService.setAuthToken(response.data.token);
      const role = this.axiosService.getRole();

      switch (role) {
        case 'SuperAdmin':
          this.router.navigate(['register-manager']);
          break;
        case 'Manager':
          this.router.navigate(['']);
          break
        case 'Guest':
          this.router.navigate(['']);
          break
        case 'Customer':
          this.router.navigate(['']);
          break;
      }
    }).catch(error => {

      console.log("Login failed:", error);
      this.axiosService.setAuthToken(null);

    });
    
  }
}
