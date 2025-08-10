import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CatService } from '../../services/cat-service';
import { Cat } from '../../models/cat-model';
import * as L from 'leaflet';

@Component({
    selector: 'app-map',
    standalone: true,
    imports: [
        CommonModule,
        MatProgressSpinnerModule
    ],
    templateUrl: './map.html',
    styleUrl: './map.scss'
})
export class MapComponent implements OnInit, AfterViewInit, OnDestroy {
    private map!: L.Map;
    private cats: Cat[] = [];
    loading = true;

    constructor(private catService: CatService) { }

    ngOnInit() {
        this.loadCats();
    }

    ngAfterViewInit() {
        this.initializeMap();
    }

    ngOnDestroy() {
        if (this.map) {
            this.map.remove();
        }
    }

    private initializeMap() {
        // Initialize map centered on Dresden
        this.map = L.map('map').setView([51.0504, 13.7373], 10);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);

        if (this.cats.length > 0) {
            this.addCatMarkers();
        }
    }

    private loadCats() {
        this.catService.getAvailableCats().subscribe({
            next: (cats) => {
                if (cats) {
                    this.cats = cats;
                    this.loading = false;

                    if (this.map) {
                        this.addCatMarkers();
                    }
                }
            },
            error: (error) => {
                console.error('Error loading cats:', error);
                this.loading = false;
            }
        });
    }

    private addCatMarkers() {
        const catIcon = L.divIcon({
            html: '<div style="background: #E08F7E; width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: whitesmoke; font-weight: bold;">🐱</div>',
            iconSize: [24, 24],
            className: 'custom-div-icon'
        });

        this.cats.forEach(cat => {
            const lat = cat.latitude;
            const lng = cat.longitude;

            const marker = L.marker([lat, lng], { icon: catIcon })
                .addTo(this.map);

            const popupContent = `
        <div class="map-popup">
          <h4>${cat.name}</h4>
          <p><strong>Breed:</strong> ${cat.breedName}</p>
          <p><strong>Age:</strong> ${cat.age} year${cat.age !== 1 ? 's' : ''}</p>
          <p><strong>Status:</strong> ${cat.adoptionStatus}</p>
          <p><strong>Location:</strong> ${cat.address}</p>
          <button class="popup-btn" onclick="window.open('/cats/${cat.id}', '_self')">
            View Details
          </button>
        </div>
      `;

            marker.bindPopup(popupContent);
        });
    }
}
