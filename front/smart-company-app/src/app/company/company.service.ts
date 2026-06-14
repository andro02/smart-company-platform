import { Injectable } from '@angular/core';
import { AxiosService } from '../auth/axios.service';

@Injectable({ providedIn: 'root' })
export class CompanyService {

  constructor(private axios: AxiosService) {}

  create(payload: any) {
    return this.axios.request('POST', '/companies', payload);
  }

  getCountries() {
    return this.axios.request('GET', '/companies/countries', null);
  }

  getCities(countryId: number) {
    return this.axios.request('GET', `/companies/cities?countryId=${countryId}`, null);
  }
}