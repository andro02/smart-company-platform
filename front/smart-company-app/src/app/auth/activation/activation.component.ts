import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AxiosService } from '../axios.service';

@Component({
  selector: 'app-activation',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activation.component.html',
  styleUrl: './activation.component.css'
})
export class ActivationComponent implements OnInit {
  activationMessage: string | null = null;

  constructor(private route: ActivatedRoute, private axios: AxiosService, private router: Router) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const token = params['token'];
      const username = params['username'];
      if (token) {
        this.activateAccount(token, username);
      } else {
        this.activationMessage = 'Invalid activation link.';
      }
    });
  }

  activateAccount(token: string, username: string): void {

    this.axios.request(
      "POST",
      "/activate/" + username + "?token=" +  token,
      null
    ).then(
      response => {
        this.activationMessage = response.data;
        this.router.navigate(['']);
      }
    );

  }

}
