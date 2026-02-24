import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class FileService {
  
  private apiUrl = 'http://localhost:8080/api/files';

  constructor(private http: HttpClient) {}

  uploadFile(key: string, username: string) {
    const formData = new FormData();
    formData.append('key', key);
    formData.append('username', username);
    return this.http.put(`${this.apiUrl}/presigned-url/put`, formData, { responseType: 'text' });
  }

  downloadFile(key: string) {
    return this.http.get(`${this.apiUrl}/presigned-url/get?key=${key}`, { responseType: 'text' });
  }

}
