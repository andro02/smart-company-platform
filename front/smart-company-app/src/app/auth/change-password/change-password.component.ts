import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { User, UserType } from '../model/user';
import { Router } from '@angular/router';
import { AxiosService } from '../axios.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [FormsModule, CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css'
})
export class ChangePasswordComponent {

  changePasswordForm: FormGroup;

  constructor(private axios: AxiosService, private formBuilder: FormBuilder, private router: Router) { 

    this.changePasswordForm = this.formBuilder.group({
      password: ["", [Validators.required, Validators.minLength(5)]],
      confirmPassword: ["", [Validators.required, Validators.minLength(5), this.passwordMatchValidator.bind(this)]],
    });
    this.changePasswordForm.get('password')?.valueChanges.subscribe(() => {
      this.changePasswordForm.get('confirmPassword')?.updateValueAndValidity();
    });

  }

  onSubmit(): void {

    if (this.changePasswordForm.invalid) {
      this.changePasswordForm.markAllAsTouched();
      return;
    }

    const submitData = this.changePasswordForm.value;

    const updatedUser = new User(
      this.axios.getUsername(), 
      submitData.password, 
      this.axios.getRole() as UserType, 
      true, 
      this.axios.getUser()!.email, 
      this.axios.getUser()!.image
    );

    this.axios.request(
      "PUT",
      "/account/" + updatedUser.username,
      updatedUser
    ).then(response => {

      this.axios.setAuthToken(response.data.token);
      this.router.navigate(['']);

    }).catch(error => {

      this.axios.setAuthToken(null);

    });
  }

  passwordMatchValidator(control: AbstractControl) {

    if (!control.parent) return null;

    const password = control.parent.get('password')?.value;
    const confirmPassword = control.value;

    return password === confirmPassword ? null : { mismatch: true };
  }

}
