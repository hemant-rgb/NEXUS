"use client";

import React, { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Eye, EyeOff, ArrowRight, AlertCircle, CheckCircle2 } from "lucide-react";
import { useAuth } from "@/lib/auth-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

export default function RegisterPage() {
  const router = useRouter();
  const { register } = useAuth();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password.length < 8) {
      setError("Password must contain at least 8 characters.");
      return;
    }

    setLoading(true);
    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
      });
      // Redirect to onboarding workflow
      router.push("/onboarding");
    } catch (err: any) {
      setError(err?.message || "Failed to create account. Please check your information.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg-main flex items-center justify-center p-6">
      <div className="w-full max-w-md space-y-6">
        {/* Brand */}
        <div className="text-center space-y-2">
          <Link href="/" className="inline-flex items-center gap-2.5">
            <div className="h-8 w-8 rounded-sm bg-brand flex items-center justify-center text-black font-mono font-bold text-base">
              N
            </div>
            <span className="font-mono font-bold text-lg tracking-wider text-text-primary">
              NEXUS
            </span>
          </Link>
          <h2 className="text-xl font-mono font-semibold text-text-primary">
            Initialize Engineer Profile
          </h2>
          <p className="text-xs font-mono text-text-muted">
            Create your account to unlock opportunity discovery and roadmaps
          </p>
        </div>

        {/* Form Card */}
        <div className="p-6 rounded-md bg-surface-card border border-surface-border space-y-5">
          {error && (
            <div className="p-3 rounded-sm bg-danger/10 border border-danger/30 flex items-start gap-2.5 text-xs text-danger font-mono">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <Input
                label="Full Name"
                placeholder="Ada Lovelace"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                autoComplete="name"
              />
            </div>

            <div>
              <Input
                label="Developer Email"
                type="email"
                placeholder="ada@lovelace.io"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>

            <div className="space-y-1.5">
              <div className="relative">
                <Input
                  id="register-password"
                  label="Master Password (min 8 characters)"
                  type={showPassword ? "text" : "password"}
                  placeholder="••••••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-2.5 top-[34px] text-text-muted hover:text-text-primary p-1"
                  tabIndex={-1}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <Button type="submit" loading={loading} className="w-full mt-2">
              Create Account &amp; Begin Onboarding
              <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </form>
        </div>

        {/* Footer Link */}
        <div className="text-center text-xs font-mono text-text-muted">
          Already registered?{" "}
          <Link href="/login" className="text-brand hover:underline font-medium">
            Sign in to existing session
          </Link>
        </div>
      </div>
    </div>
  );
}
