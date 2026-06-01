import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ProductService } from '../product.service';
import { FileService } from '../../file.service';
import { AxiosService } from '../../auth/axios.service';
import { Product, Category, ProductSearchParams } from '../product.model';
import { UserType } from '../../auth/model/user';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css'
})
export class ProductListComponent implements OnInit, OnDestroy {

  products: Product[] = [];
  productImages: Map<number, string> = new Map();
  categories: Category[] = [];

  // Filters
  searchText = '';
  selectedCategoryId: number | null = null;
  isForSale: boolean | null = null;
  minPrice: number | null = null;
  maxPrice: number | null = null;
  sortBy = 'name';
  sortDir = 'asc';

  // Pagination
  currentPage = 0;
  pageSize = 12;
  totalPages = 0;
  totalElements = 0;

  loading = false;
  deleteError = '';

  private searchSubject = new Subject<string>();

  constructor(
    private productService: ProductService,
    private fileService: FileService,
    public axios: AxiosService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();

    // Debounce search input čeka 400ms nakon što korisnik prestane da kuca
    this.searchSubject.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(() => {
      this.currentPage = 0;
      this.loadProducts();
    });
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  loadCategories(): void {
    this.productService.getCategories().then((res: any) => {
      this.categories = res.data;
    });
  }

  loadProducts(): void {
    this.loading = true;
    const params: ProductSearchParams = {
      page: this.currentPage,
      size: this.pageSize,
      sortBy: this.sortBy,
      sortDir: this.sortDir
    };
    if (this.searchText)              params.search = this.searchText;
    if (this.selectedCategoryId)      params.categoryId = this.selectedCategoryId;
    if (this.isForSale != null)       params.isForSale = this.isForSale;
    if (this.minPrice != null)        params.minPrice = this.minPrice;
    if (this.maxPrice != null)        params.maxPrice = this.maxPrice;

    this.productService.search(params).then((res: any) => {
      this.products = res.data.content;
      this.totalPages = res.data.totalPages;
      this.totalElements = res.data.totalElements;
      this.loadProductImages();
    }).finally(() => {
      this.loading = false;
    });
  }

  loadProductImages(): void {
    this.products.forEach(product => {
      if (product.image && !this.productImages.has(product.id)) {
        this.fileService.getDownloadUrl(product.image).subscribe({
          next: (url) => this.productImages.set(product.id, url),
          error: () => {}
        });
      }
    });
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchText);
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  onSortChange(sortBy: string): void {
    if (this.sortBy === sortBy) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = sortBy;
      this.sortDir = 'asc';
    }
    this.currentPage = 0;
    this.loadProducts();
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadProducts();
  }

  resetFilters(): void {
    this.searchText = '';
    this.selectedCategoryId = null;
    this.isForSale = null;
    this.minPrice = null;
    this.maxPrice = null;
    this.sortBy = 'name';
    this.sortDir = 'asc';
    this.currentPage = 0;
    this.loadProducts();
  }

  deleteProduct(id: number): void {
    if (!confirm('Are you sure you want to delete this product?')) return;
    this.productService.delete(id).then(() => {
      this.loadProducts();
    }).catch(() => {
      this.deleteError = 'Failed to delete product.';
    });
  }

  isManager(): boolean {
    const role = this.axios.getRole();
    return role === UserType.Manager || role === UserType.SuperAdmin;
  }

  getPages(): number[] {
    const pages = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages - 1, this.currentPage + 2);
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }
}