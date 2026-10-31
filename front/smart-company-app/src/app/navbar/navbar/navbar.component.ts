import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterModule } from '@angular/router';
import { AxiosService } from '../../auth/axios.service';
import { CommonModule } from '@angular/common';
import { SuperadminNavbarComponent } from '../superadmin-navbar/superadmin-navbar.component';
import { ManagerNavbarComponent } from '../manager-navbar/manager-navbar.component';
import { FileService } from '../../file.service';
import { CustomerNavbarComponent } from '../customer-navbar/customer-navbar.component';

// shown for accounts without a profile picture, or when it can't be loaded:
// a simple person silhouette in a grey circle
const DEFAULT_AVATAR = 'data:image/svg+xml;utf8,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40">' +
  '<circle cx="20" cy="20" r="20" fill="#d5d9de"/>' +
  '<circle cx="20" cy="15" r="7" fill="#8a939e"/>' +
  '<path d="M7 34c2-7 7-10 13-10s11 3 13 10a20 20 0 0 1-26 0z" fill="#8a939e"/>' +
  '</svg>'
);

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
    // accounts without a profile picture (e.g. the demo accounts); the token
    // carries a missing picture as the text "null"
    if (user != null && (!user.image || user.image === 'null')) {
      this.preSignedUrl = DEFAULT_AVATAR;
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
      this.preSignedUrl = DEFAULT_AVATAR;
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