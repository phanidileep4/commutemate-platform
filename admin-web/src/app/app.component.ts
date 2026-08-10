import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';

type Location = {id:string,name:string,timezone:string};
@Component({selector:'app-root',standalone:true,imports:[CommonModule],templateUrl:'./app.component.html',styleUrl:'./app.component.css'})
export class AppComponent {
  private http=inject(HttpClient); api='http://localhost:8080/api/v1';
  tenant=signal('northstar'); email=signal('admin@northstar.example');
  locations=signal<Location[]>([]); status=signal('Demo data');
  weights=signal({route:30,schedule:20,preference:15,social:10,history:10,parking:10,reliability:5});
  metrics=[['Peak vehicles avoided','312 / day'],['Shared commutes','1,842'],['Positive ride feedback','91%'],['Cross-team connections','126']];
  headers(){return new HttpHeaders({'X-Tenant-Slug':this.tenant(),'X-User-Email':this.email()});}
  load(){this.http.get<Location[]>(`${this.api}/admin/locations`,{headers:this.headers()}).subscribe({next:v=>{this.locations.set(v);this.status.set('Connected to API')},error:()=>this.status.set('API unavailable — showing demo dashboard')});}
}
