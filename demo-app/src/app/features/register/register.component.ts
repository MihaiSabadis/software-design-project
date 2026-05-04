import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginStore } from '../login/login.store';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    RouterLink,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);

  protected readonly isSubmitting = this.loginStore.isSubmitting;
  protected readonly errorMessage = this.loginStore.errorMessage;

  protected readonly registerForm = this.formBuilder.group({
    role: ['PLAYER', Validators.required],
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    age: [0, [Validators.required, Validators.min(1)]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  onSubmit() {
    if (this.registerForm.invalid || this.isSubmitting()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const formData = this.registerForm.getRawValue();

    this.authService.register(formData).subscribe({
      next: () => {
        this.loginStore
          .login({ email: formData.email, password: formData.password })
          .subscribe((loginResult) => {
            if (loginResult.success) {
              if (loginResult.role === 'ADMIN') {
                void this.router.navigate(['/people']);
              } else {
                void this.router.navigate(['/player']);
              }
            }
          });
      },
      error: (err) => {
        console.error('Registration failed', err);
      },
    });
  }
}
