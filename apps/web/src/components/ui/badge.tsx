import React from "react";
import { cn } from "@/lib/utils";

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: "brand" | "secondary" | "outline" | "cyan" | "warning" | "success";
}

export function Badge({ className, variant = "secondary", children, ...props }: BadgeProps) {
  const variants = {
    brand: "bg-brand-subtle text-brand border-brand/30",
    secondary: "bg-surface-hover text-text-secondary border-surface-border",
    outline: "bg-transparent text-text-secondary border-surface-border",
    cyan: "bg-cyan-subtle text-cyan border-cyan/30",
    warning: "bg-warning-subtle text-warning border-warning/30",
    success: "bg-success-subtle text-success border-success/30",
  };

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 px-2 py-0.5 rounded-sm text-[10px] font-mono uppercase tracking-wider font-medium border",
        variants[variant],
        className
      )}
      {...props}
    >
      {children}
    </span>
  );
}
