import { Component, inject, signal} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterLink } from '@angular/router';
import {MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { MatCard } from '@angular/material/card';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'app-password-reset',
  imports: [
    ReactiveFormsModule,
    MatLabel,
    MatFormField,
    MatCard,
    RouterLink,
    MatInput,
    MatButton,
    MatError,
  ],
  templateUrl: './password-reset.component.html',
  styleUrl: './password-reset.component.scss',
})
export class PasswordResetComponent {
  private formBuilder = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  step = signal<1 | 2>(1);
  isSubmitting = signal(false);

  emailForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
  });

  resetForm = this.formBuilder.group(
    {
      code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    }, { validators: this.passwordMatchValidator });

  passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
    const password = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  }

  requestResetCode() {
    if (this.emailForm.invalid) return;
    this.isSubmitting.set(true);

    const email = this.emailForm.getRawValue().email!;

    this.authService.forgotPassword(email).subscribe({
      next: () => {
        this.step.set(2);
        this.isSubmitting.set(false);
      },
      error: (err) => {
        this.isSubmitting.set(false);
      },
    });
  }

  submitNewPassword() {
    if (this.resetForm.invalid) return;
    this.isSubmitting.set(true);

    const email = this.emailForm.getRawValue().email!;
    const { code, newPassword } = this.resetForm.getRawValue();

    this.authService.resetPassword(email, code!, newPassword!).subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
      },
    });
  }
}
