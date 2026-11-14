import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/lib/auth-context";

export const metadata: Metadata = {
  title: "NEXUS — Opportunity Intelligence & Execution Platform",
  description: "Next-generation opportunity intelligence, skill matching, and execution platform for software engineers.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="bg-bg-main text-text-primary antialiased selection:bg-brand selection:text-black">
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
