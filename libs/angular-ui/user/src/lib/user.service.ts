import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ChangePasswordRequest, UpdateUserRequest, UserResponse } from '@bootstrapbugz/shared';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  API_URL = 'http://localhost:8181/v1.0';

  constructor(private http: HttpClient) {}

  findByUsername(username: string) {
    return this.http.get<UserResponse>(`${this.API_URL}/users/${username}`);
  }

  changePassword(changePasswordRequest: ChangePasswordRequest) {
    return this.http.put<void>(`${this.API_URL}/users/change-password`, changePasswordRequest);
  }

  updateUser(updateUser: UpdateUserRequest) {
    return this.http.put<UserResponse>(`${this.API_URL}/users/update`, updateUser);
  }
}
