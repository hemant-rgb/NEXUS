import { test, expect } from "@playwright/test";

test.describe("Execution & Intelligence Journeys (Phases 6 & 7)", () => {
  test("Journey F: GitHub OS, Contribution Workspace & Issue Discovery", async ({ page }) => {
    const timestamp = Date.now();
    const user = {
      name: `Engineer ${timestamp}`,
      email: `engineer-${timestamp}@nexus.io`,
      password: "Password123!",
    };

    // 1. Register engineer account
    await page.goto("/register");
    await page.waitForLoadState("networkidle");
    await page.fill('input[autocomplete="name"]', user.name);
    await page.fill('input[autocomplete="email"]', user.email);
    await page.fill('input[autocomplete="new-password"]', user.password);
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/.*(onboarding|dashboard)/, { timeout: 10000 });

    // 2. Navigate to GitHub Intelligence (/github)
    await page.goto("/github");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("main h1")).toContainText(/GitHub & Contribution Intelligence/i);

    // 3. Connect Mock GitHub Token
    const tokenInput = page.locator('input[placeholder*="ghp_"]');
    if (await tokenInput.isVisible()) {
      await tokenInput.fill("ghp_mockDeveloperTokenForAudit");
      await page.click('button:has-text("Connect Account")');
      await expect(page.locator("text=AES-256-GCM Encrypted")).toBeVisible({ timeout: 5000 });
    }

    // 4. Verify tabs: Issues & Repositories
    await expect(page.locator('button:has-text("Issue Discovery & Match")')).toBeVisible();
    await expect(page.locator('button:has-text("Curated Repositories")')).toBeVisible();

    // 5. Switch to Curated Repositories tab
    await page.click('button:has-text("Curated Repositories")');
    await expect(page.locator("text=Branch:").first()).toBeVisible({ timeout: 5000 });
  });

  test("Journey G: Application Pipeline Kanban, Resumes & Document Workspace", async ({ page }) => {
    const timestamp = Date.now() + 1000;
    const user = {
      name: `Applicant ${timestamp}`,
      email: `applicant-${timestamp}@nexus.io`,
      password: "Password123!",
    };

    // 1. Register candidate account
    await page.goto("/register");
    await page.waitForLoadState("networkidle");
    await page.fill('input[autocomplete="name"]', user.name);
    await page.fill('input[autocomplete="email"]', user.email);
    await page.fill('input[autocomplete="new-password"]', user.password);
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL(/.*(dashboard|onboarding)/, { timeout: 10000 });

    // 2. Navigate to Applications Pipeline
    await page.goto("/applications");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("main h1")).toContainText(/Application Pipeline & Execution/i);

    // 3. Verify Pipeline KPI metrics banner
    await expect(page.locator("text=Total Applications")).toBeVisible();
    await expect(page.locator("text=Active In-Flight")).toBeVisible();

    // 4. Navigate to Resumes Console
    await page.goto("/resumes");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("main h1")).toContainText(/Resume Management/i);
    await expect(page.locator('button:has-text("Create")').first()).toBeVisible();

    // 5. Navigate to New Application form
    await page.goto("/applications/new");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("main h1")).toContainText(/New Application Tracker/i);

    // Fill new application form
    await page.fill('input[placeholder*="Google, Stripe"]', "Acme Labs");
    await page.fill('input[placeholder*="Software Engineering Intern"]', "Systems Engineering Intern");
    await page.click('button:has-text("Create Application")');

    // Verify redirected back to applications pipeline
    await expect(page).toHaveURL(/.*applications/, { timeout: 10000 });
    await expect(page.locator("text=Systems Engineering Intern").first()).toBeVisible({ timeout: 5000 });
  });
});
