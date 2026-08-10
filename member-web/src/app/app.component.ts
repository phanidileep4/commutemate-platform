import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, inject } from '@angular/core';

type RideOffer={id:string,departureAt:string,commuteIntent:string,seatsAvailable:number};
@Component({selector:'app-root',standalone:true,imports:[CommonModule],templateUrl:'./app.component.html',styleUrl:'./app.component.css'})
export class AppComponent {
  private http=inject(HttpClient); api='http://localhost:8080/api/v1'; intent='Quiet'; status='Demo recommendations';
  intents=['Quiet','Social','Networking','Fastest','Max impact'];
  matches=[{name:'Sam',score:94,route:97,time:'8:05 AM',why:'Great route + strong repeat-ride fit'},{name:'Alex',score:91,route:99,time:'8:10 AM',why:'Best route + similar commute style'},{name:'Jordan',score:84,route:100,time:'7:55 AM',why:'Fastest route + parking impact'}];
  connect(){const headers=new HttpHeaders({'X-Tenant-Slug':'northstar','X-User-Email':'member@northstar.example'});this.http.get(`${this.api}/me`,{headers}).subscribe({next:()=>this.status='Connected to tenant API',error:()=>this.status='API unavailable — demo mode'});}
}
