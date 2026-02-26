import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators, AbstractControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AxiosService } from '../../auth/axios.service';
import { UserType, User } from '../../auth/model/user';
import { CommonModule } from '@angular/common';
import { FileService } from '../../file.service';
import { HttpClient } from '@angular/common/http';
import { passwordMatchValidator } from '../../common/validators/password-match.validator';

@Component({
  selector: 'app-register-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './register-manager.component.html',
  styleUrl: './register-manager.component.css'
})
export class RegisterManagerComponent {

  selectedFile: File | null = null;
  preSignedUrl: string = '';
  
  fileError: boolean = false;
  errorMessage: string = '';

  formSubmitted: boolean = false;

  registerForm: FormGroup;

  constructor(public axios: AxiosService, private formBuilder: FormBuilder, private router: Router, private fileService: FileService, private http: HttpClient) {

    this.registerForm = this.formBuilder.group(
      {
        email: ["", Validators.compose([Validators.required, Validators.email])],
        password: ["", Validators.compose([Validators.required, Validators.minLength(5)])],
        confirmPassword: ["", Validators.compose([Validators.required, Validators.minLength(5)])],
        username: ["", Validators.compose([Validators.required, Validators.minLength(5)])],
        file: [null, Validators.compose([Validators.required])],
        // type: ["Guest", [Validators.required]]
      },
      {
        validators: passwordMatchValidator('password', 'confirmPassword')
      }
    );

  }

  onFileSelected(event: any, label: HTMLLabelElement) {

    const file = event.target.files?.[0];

    // Ako nema fajla
    if (!file) {
      label.classList.remove('selected');
      label.innerText = 'Choose file';
      this.selectedFile = null;
      return;
    }

    const validTypes = [
      'image/jpeg',
      'image/png',
      'image/gif'
    ];

    // Ako tip nije validan
    if (!validTypes.includes(file.type)) {

      label.classList.remove('selected');
      label.innerText = 'Choose file';

      this.fileError = true;
      this.errorMessage = 'Invalid file type! Please select (.jpg, .png, .gif)';

      this.selectedFile = null;
      this.registerForm.get('file')?.setValue(null);

      return;
    }

    // Ako je sve OK
    label.classList.add('selected');
    label.innerText = file.name;

    this.fileError = false;
    this.errorMessage = '';

    this.selectedFile = file;
  }

  onSubmit(): void {

    this.formSubmitted = true;
    this.registerForm.markAllAsTouched();

    if (this.registerForm.invalid || !this.selectedFile) {
      return;
    }

    const registerData = this.registerForm.value;

    this.fileService.uploadFile(this.selectedFile.name, registerData.username)
      .subscribe({
        next: (response) => {

          const res = JSON.parse(response);

          const headers = {
            'Content-Type': this.selectedFile!.type
          }

          this.http.put(res.url, this.selectedFile, { headers })
            .subscribe({
              next: () => {

                const user = new User(
                  registerData.username,
                  registerData.password,
                  UserType.Manager,
                  false,
                  registerData.email,
                  res.filename
                );

                this.axios.request('POST', '/register', user)
                  .then(res => {
                    this.router.navigate(['register-manager']);
                  })
                  .catch(err => {
                    console.log(err.response?.data);
                  });

              }
            });

        },
        error: err => console.error(err)
      })
  }
}
