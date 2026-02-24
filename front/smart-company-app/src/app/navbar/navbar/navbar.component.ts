import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterModule } from '@angular/router';
import { AxiosService } from '../../auth/axios.service';
import { CommonModule } from '@angular/common';
import { SuperadminNavbarComponent } from '../superadmin-navbar/superadmin-navbar.component';
import { FileService } from '../../file.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule, SuperadminNavbarComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {

  preSignedUrl: string | null = null;

  constructor(public axios: AxiosService, private router: Router, public fileService: FileService, private route: ActivatedRoute) {

    this.fetchImage();

  }

  ngOnInit(): void {

    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.fetchImage();
      }
    });

  }

  fetchImage(): void {

    if (this.axios.getUser() != null) {
      this.fileService.downloadFile(this.axios.getUser()!.image).subscribe({
        next: (response) => {
          this.preSignedUrl = response;
        },
        error: (err) => {
          console.log(err);
        }
      });
    }

  }

  logOut(): void {
    this.axios.setAuthToken(null);
    this.router.navigate(['login']);
    this.preSignedUrl = null;
  }

}
