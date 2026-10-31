import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterModule } from '@angular/router';
import { AxiosService } from '../../auth/axios.service';
import { CommonModule } from '@angular/common';
import { SuperadminNavbarComponent } from '../superadmin-navbar/superadmin-navbar.component';
import { ManagerNavbarComponent } from '../manager-navbar/manager-navbar.component';
import { FileService } from '../../file.service';
import { CustomerNavbarComponent } from '../customer-navbar/customer-navbar.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule, SuperadminNavbarComponent, ManagerNavbarComponent, CustomerNavbarComponent],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {

  preSignedUrl: string | null = null;
  // whether the current picture already failed once (see onImageError)
  imageRetried = false;

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
    const user = this.axios.getUser();
    // accounts without a profile picture (e.g. the demo accounts) show none
    if (user != null && !user.image) {
      this.preSignedUrl = null;
      return;
    }
    if (user != null) {
      this.fileService.getDownloadUrl(user.image).subscribe({
        next: (response: string) => { this.preSignedUrl = response; },
        error: (err: any) => { console.log(err); }
      });
    }
  }

  onImageError(): void {
    // the presigned URL may have expired, so ask for a new one, but only once:
    // a picture that is really missing would otherwise be requested endlessly
    if (this.imageRetried) {
      this.preSignedUrl = null;
      return;
    }
    this.imageRetried = true;
    this.fetchImage();
  }

  logOut(): void {
    this.axios.setAuthToken(null);
    this.router.navigate(['login']);
    this.preSignedUrl = null;
  }
}