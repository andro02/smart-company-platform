import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { VehicleService } from '../vehicle.service';
import { FileService } from '../../file.service';
import { Vehicle } from '../vehicle.model';

@Component({
  selector: 'app-vehicle-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './vehicle-detail.component.html',
})
export class VehicleDetailComponent implements OnInit, OnDestroy {

  vehicle: Vehicle | null = null;
  imageUrls: string[] = [];
  loading = true;
  error = '';

  currentImageIndex = 0;
  private galleryInterval: any;

  constructor(
    private route: ActivatedRoute,
    private vehicleService: VehicleService,
    private fileService: FileService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  ngOnDestroy(): void {
    if (this.galleryInterval) clearInterval(this.galleryInterval);
  }

  load(id: number): void {
    this.loading = true;
    this.vehicleService.getById(id).then((res: any) => {
      this.vehicle = res.data as Vehicle;
      this.loadImages();
      if (this.vehicle.latitude && this.vehicle.longitude) {
        setTimeout(() => this.initMap(), 100);
      }
    }).catch(() => {
      this.error = 'Failed to load vehicle.';
    }).finally(() => this.loading = false);
  }

  private async initMap(): Promise<void> {
    if (!this.vehicle?.latitude || !this.vehicle?.longitude) return;
    const L = await import('leaflet');

    const iconDefault = L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41], iconAnchor: [12, 41]
    });
    L.Marker.prototype.options.icon = iconDefault;

    const map = L.map('detail-map', { zoomControl: true, dragging: false, scrollWheelZoom: false })
      .setView([this.vehicle.latitude, this.vehicle.longitude], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: 'Â© OpenStreetMap'
    }).addTo(map);
    L.marker([this.vehicle.latitude, this.vehicle.longitude]).addTo(map);
  }

  loadImages(): void {
    if (!this.vehicle?.imageKeys?.length) return;
    this.vehicle.imageKeys.forEach(key => {
      this.fileService.getDownloadUrl(key).subscribe({
        next: (url) => this.imageUrls.push(url),
        error: () => {}
      });
    });
    if (this.vehicle.imageKeys.length > 1) {
      this.galleryInterval = setInterval(() => {
        this.currentImageIndex = (this.currentImageIndex + 1) % this.imageUrls.length;
      }, 4000);
    }
  }

  formatHeartbeat(ts: string | null): string {
    if (!ts) return 'Never';
    const d = new Date(ts);
    return isNaN(d.getTime()) ? 'Unknown' : d.toLocaleString();
  }
}