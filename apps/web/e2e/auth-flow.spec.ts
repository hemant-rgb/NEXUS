import { test, expect } from "@playwright/test";

test.describe("Critical E2E Authentication and Onboarding Flow", () => {
  const timestamp = Date.now();
  const testUser = {
    name: `Engineer ${timestamp}`,
    email: `dev-${timestamp}@nexus.io`,
    password: "Password123!",
  };

  test("Registration -> Onboarding -> Skills -> Dashboard -> Persistence -> Logout", async ({ page }) => {
    // 1. Open landing page and navigate to register
    await page.goto("/");
    await expect(page).toHaveTitle(/NEXUS/i);
    await page.click("text=Get Started");

    // 2. Register new developer account
    await expect(page).toHaveURL(/.*register/);
    await page.fill('input[autocomplete="name"]', testUser.name);
    await page.fill('input[autocomplete="email"]', testUser.email);
    await page.fill('input[autocomplete="new-password"]', testUser.password);
    await page.click('button[type="submit"]');

    // 3. Arrive at Onboarding Wizard Step 1
    await expect(page).toHaveURL(/.*onboarding/, { timeout: 10000 });
    await expect(page.locator("text=Profile Onboarding")).toBeVisible();

    // Fill Step 1 details
    await page.fill('input[placeholder*="Berkeley"]', "MIT");
    await page.fill('input[placeholder*="octocat"]', `gh-${timestamp}`);
    await page.click('button:has-text("Continue")');

    // 4. Step 2 (Education)
    await expect(page.locator("text=Academic Background")).toBeVisible();
    await page.click('button:has-text("Continue")');

    // 5. Step 3 (Skills)
    await expect(page.locator("text=Technical Skills Matrix")).toBeVisible();
    // Add TypeScript from catalog if visible or custom
    const tsButton = page.locator('button:has-text("TypeScript")').first();
    if (await tsButton.isVisible()) {
      await tsButton.click();
    }
    await page.click('button:has-text("Continue")');

    // 6. Step 4 (Interests)
    await expect(page.locator("text=Professional & Technical Interests")).toBeVisible();
    await page.click('button:has-text("Continue")');

    // 7. Step 5 (Preferences)
    await expect(page.locator("text=Opportunity Preferences")).toBeVisible();
    await page.click('button:has-text("Continue")');

    // 8. Step 6 (Review & Finalize)
    await expect(page.locator("text=Profile Summary & Verification")).toBeVisible();
    await page.click('button:has-text("Complete Onboarding & Enter Console")');

    // 9. Arrive at Dashboard Command Center
    await expect(page).toHaveURL(/.*dashboard/, { timeout: 10000 });
    await expect(page.locator(`text=${testUser.name}`)).toBeVisible();

    // 10. Verify Data Persists After Reload
    await page.reload();
    await expect(page.locator(`text=${testUser.name}`)).toBeVisible();

    // 11. Log Out
    await page.click('button[title="Log out"]');
    await expect(page).toHaveURL(/.*login/, { timeout: 10000 });

    // 12. Verify Protected Pages are Inaccessible
    await page.goto("/dashboard");
    await expect(page).toHaveURL(/.*login/, { timeout: 10000 });
  });
});
