import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class FileService {

  private apiUrl = 'http://localhost:8080/api/files';
  private urlCache = new Map<string, string>();

  constructor(private http: HttpClient) {}

  // Returns { url: string, filename: string } (PreSignedUrlDto)
  getUploadUrl(filename: string, folder: string) {
    return this.http.put(
      `${this.apiUrl}/presigned-url/put?filename=${encodeURIComponent(filename)}&folder=${encodeURIComponent(folder)}`,
      null,
      { responseType: 'text' }
    );
  }

  uploadToS3(presignedUrl: string, file: File) {
    return this.http.put(presignedUrl, file, {
      headers: { 'Content-Type': file.type }
    });
  }

  getDownloadUrl(key: string): Observable<string> {
    if (this.urlCache.has(key)) {
      return of(this.urlCache.get(key)!);
    }
    return this.http.get(
      `${this.apiUrl}/presigned-url/get?key=${encodeURIComponent(key)}`,
      { responseType: 'text' }
    ).pipe(
      tap(url => this.urlCache.set(key, url))
    );
  }
}