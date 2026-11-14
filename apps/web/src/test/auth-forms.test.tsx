import { describe, it, expect, vi } from "vitest";
import React from "react";
import { render, screen, fireEvent } from "@testing-library/react";
import LoginPage from "../app/(auth)/login/page";
import RegisterPage from "../app/(auth)/register/page";

// Mock router and auth-context
vi.mock("next/navigation", () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn(),
  }),
}));

const mockLogin = vi.fn();
const mockRegister = vi.fn();

vi.mock("@/lib/auth-context", () => ({
  useAuth: () => ({
    login: mockLogin,
    register: mockRegister,
    user: null,
    loading: false,
  }),
}));

describe("Authentication Forms", () => {
  it("renders login form with email, password and submit button", () => {
    render(<LoginPage />);
    expect(screen.getByLabelText(/Developer Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Sign In to Console/i })).toBeInTheDocument();
  });

  it("renders registration form with full name, email and password", () => {
    render(<RegisterPage />);
    expect(screen.getByLabelText(/Full Name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Developer Email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Master Password/i)).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Create Account & Begin Onboarding/i })
    ).toBeInTheDocument();
  });

  it("validates short password on registration", async () => {
    render(<RegisterPage />);
    const nameInput = screen.getByLabelText(/Full Name/i);
    const emailInput = screen.getByLabelText(/Developer Email/i);
    const passwordInput = screen.getByLabelText(/Master Password/i);
    const submitBtn = screen.getByRole("button", { name: /Create Account & Begin Onboarding/i });

    fireEvent.change(nameInput, { target: { value: "Linus Torvalds" } });
    fireEvent.change(emailInput, { target: { value: "linus@kernel.org" } });
    fireEvent.change(passwordInput, { target: { value: "short" } });

    fireEvent.click(submitBtn);

    expect(
      await screen.findByText(/Password must contain at least 8 characters/i)
    ).toBeInTheDocument();
    expect(mockRegister).not.toHaveBeenCalled();
  });
});
