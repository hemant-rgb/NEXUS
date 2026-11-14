import React from "react";
import { cn } from "@/lib/utils";

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline" | "ghost" | "danger";
  size?: "sm" | "md" | "lg";
  loading?: boolean;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", size = "md", loading = false, disabled, children, ...props }, ref) => {
    const baseStyles =
      "inline-flex items-center justify-center font-mono text-xs uppercase tracking-wider font-semibold transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-brand disabled:pointer-events-none disabled:opacity-50 select-none";

    const variants = {
      primary: "bg-brand text-black hover:bg-brand-hover shadow-sm",
      secondary: "bg-surface-hover text-text-primary hover:bg-surface-border border border-surface-border",
      outline: "border border-surface-border bg-transparent text-text-primary hover:bg-surface-hover hover:border-text-secondary",
      ghost: "text-text-secondary hover:text-text-primary hover:bg-surface-hover",
      danger: "bg-danger text-black hover:bg-red-500",
    };

    const sizes = {
      sm: "h-8 px-3 rounded-sm text-[11px]",
      md: "h-9 px-4 rounded-md text-xs",
      lg: "h-11 px-6 rounded-md text-sm",
    };

    return (
      <button
        ref={ref}
        disabled={disabled || loading}
        className={cn(baseStyles, variants[variant], sizes[size], className)}
        {...props}
      >
        {loading ? (
          <span className="flex items-center gap-2">
            <svg
              className="h-3.5 w-3.5 animate-spin text-current"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path
                className="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            <span>Processing...</span>
          </span>
        ) : (
          children
        )}
      </button>
    );
  }
);
Button.displayName = "Button";
