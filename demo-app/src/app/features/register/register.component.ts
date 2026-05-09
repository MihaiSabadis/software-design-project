// demo-app/src/app/features/register/register.component.ts
import { ChangeDetectionStrategy, Component, inject, signal, computed } from '@angular/core';
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
  changeDetection: ChangeDetectionStrategy.OnPush,
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
    studioCode: [''],
  });

  // Reactive signal so the template can react without subscribing
  protected readonly selectedRole = signal(this.registerForm.controls.role.value);

  protected readonly isModerator = computed(() => this.selectedRole() === 'MODERATOR');

  constructor() {
    // Keep signal in sync with form control
    this.registerForm.controls.role.valueChanges.subscribe((role) => {
      this.selectedRole.set(role);
      const studioCodeCtrl = this.registerForm.controls.studioCode;

      if (role === 'MODERATOR') {
        studioCodeCtrl.setValidators([Validators.required, Validators.minLength(6)]);
      } else {
        studioCodeCtrl.clearValidators();
        studioCodeCtrl.setValue('');
      }
      studioCodeCtrl.updateValueAndValidity();
    });
  }

  protected onSubmit(): void {
    if (this.registerForm.invalid || this.isSubmitting()) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const formData = this.registerForm.getRawValue();

    this.authService.register(formData).subscribe({
      next: () => {
        this.loginStore
          .login({ email: formData.email, password: formData.password })
          .subscribe((result) => {
            if (result.success) {
              const destination =
                result.role === 'ADMIN'
                  ? '/people'
                  : result.role === 'MODERATOR'
                    ? '/games'
                    : '/games';
              void this.router.navigate([destination]);
            }
          });
      },
      error: (err) => {
        console.error('Registration failed', err);
      },
    });
  }
}
