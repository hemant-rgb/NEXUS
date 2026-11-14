import { describe, it, expect } from "vitest";
import React from "react";
import { render, screen } from "@testing-library/react";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Badge } from "../components/ui/badge";

describe("UI Components", () => {
  it("renders Button with primary variant and text", () => {
    render(<Button variant="primary">Launch System</Button>);
    const btn = screen.getByRole("button", { name: /Launch System/i });
    expect(btn).toBeInTheDocument();
    expect(btn).toHaveClass("bg-brand");
  });

  it("renders Button loading state with spinner", () => {
    render(<Button loading>Processing</Button>);
    expect(screen.getByText(/Processing.../i)).toBeInTheDocument();
  });

  it("renders Input with label, placeholder and error message", () => {
    render(
      <Input
        label="Username"
        placeholder="Enter username"
        error="Field is required"
      />
    );
    expect(screen.getByText("Username")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Enter username")).toBeInTheDocument();
    expect(screen.getByText("Field is required")).toBeInTheDocument();
  });

  it("renders Badge with custom variants", () => {
    render(<Badge variant="brand">Verified</Badge>);
    expect(screen.getByText("Verified")).toBeInTheDocument();
    expect(screen.getByText("Verified")).toHaveClass("text-brand");
  });
});
