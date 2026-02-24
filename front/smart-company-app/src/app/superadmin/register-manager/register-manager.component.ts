import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AxiosService } from '../../auth/axios.service';
import { UserType, User } from '../../auth/model/user';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-register-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './register-manager.component.html',
  styleUrl: './register-manager.component.css'
})
export class RegisterManagerComponent {

  registerForm: FormGroup;

  constructor(public axios: AxiosService, private formBuilder: FormBuilder, private router: Router) {

    this.registerForm = this.formBuilder.group({
      email: ["", Validators.compose([Validators.required, Validators.email])],
      password: ["", Validators.compose([Validators.required, Validators.minLength(5)])],
      confirmPassword: ["", Validators.compose([Validators.required, Validators.minLength(5), this.passwordMatchValidator.bind(this)])],
      username: ["", Validators.compose([Validators.required, Validators.minLength(5)])],
      // type: ["Guest", [Validators.required]]
    });
    
    this.registerForm.get('password')?.valueChanges.subscribe(() => {
      this.registerForm.get('confirmPassword')?.updateValueAndValidity();
    });

  }

  passwordMatchValidator(control: AbstractControl) {
    if (!control.parent) return null;

    const password = control.parent.get('password')?.value;
    const confirmPassword = control.value;

    return password === confirmPassword ? null : { mismatch: true };
  }

  onSubmit(): void {

    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const registerData = this.registerForm.value;

    const user = new User(
      registerData.username,
      registerData.password,
      UserType.Manager,
      true,
      registerData.email,
      "profile.jpg"
    );

    this.axios.request(
      "POST",
      "/register",
      user
    ).then(response => {

      this.router.navigate(['register-manager']);

    }).catch(error => {

      console.log(error.response?.data);

    });
  }
}
