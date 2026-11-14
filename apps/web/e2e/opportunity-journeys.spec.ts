import { test, expect } from "@playwright/test";

test.describe("Opportunity Discovery and Management Journeys", () => {
  const timestamp = Date.now();
  const userA = {
    name: `User Alpha ${timestamp}`,
    email: `alpha-${timestamp}@nexus.io`,
    password: "Password123!",
  };

  const userB = {
    name: `User Beta ${timestamp}`,
    email: `beta-${timestamp}@nexus.io`,
    password: "Password123!",
  };

  test("Journey B: Discover, Filter, Save, Note & Delete Opportunity", async ({ page }) => {
    // 1. Register User A first so they can save opportunities
    await page.goto("/register");
    await page.waitForLoadState("networkidle");
    await page.fill('input[autocomplete="name"]', userA.name);
    await page.fill('input[autocomplete="email"]', userA.email);
    await page.fill('input[autocomplete="new-password"]', userA.password);
    await page.click('button[type="submit"]');

    // Wait for redirect to onboarding or dashboard
    await expect(page).toHaveURL(/.*(onboarding|dashboard)/, { timeout: 10000 });

    // 2. Open Discovery page
    await page.goto("/opportunities");
    await expect(page).toHaveURL(/.*opportunities/);
    await expect(page.locator("main h1")).toContainText(/Opportunity Discovery/i);

    // 3. Search for "Google" and filter by "Google Summer of Code"
    await page.fill('input[placeholder*="Search opportunities"]', "Google");
    await page.click('button:has-text("Google Summer of Code")');

    // 4. Verify filtered result card is visible
    const oppCard = page.locator('text=Google Summer of Code 2027 Contributor').first();
    await expect(oppCard).toBeVisible({ timeout: 5000 });

    // 5. Click Inspect to open Opportunity Details view
    const inspectBtn = page.locator('button:has-text("Inspect")').first();
    await inspectBtn.click();
    await expect(page).toHaveURL(/.*opportunities\/[a-zA-Z0-9-]+/, { timeout: 10000 });
    await expect(page.locator("main h1")).toContainText("Google Summer of Code 2027 Contributor");

    // 6. Save Opportunity
    const saveButton = page.locator('button:has-text("Save Opportunity")');
    await expect(saveButton).toBeVisible();
    await saveButton.click();

    // 7. Add personal notes
    const notesInput = page.locator('textarea[placeholder*="preparation checklist"]');
    await expect(notesInput).toBeVisible();
    await notesInput.fill("Prepare contribution proposal by March 15th.");
    await page.click('button:has-text("Save Personal Notes")');
    await expect(page.locator("text=Saved!").first()).toBeVisible();

    // 8. Open Saved Opportunities page
    await page.goto("/saved");
    await expect(page).toHaveURL(/.*saved/);
    await expect(page.locator("main h1")).toContainText(/Saved Opportunities/i);
    await expect(page.locator("text=Google Summer of Code 2027 Contributor")).toBeVisible();
    await expect(page.locator("text=Prepare contribution proposal by March 15th.")).toBeVisible();

    // 9. Reload and verify persistence
    await page.reload();
    await expect(page.locator("text=Google Summer of Code 2027 Contributor")).toBeVisible();
    await expect(page.locator("text=Prepare contribution proposal by March 15th.")).toBeVisible();

    // 10. Remove saved opportunity
    const unsaveButton = page.locator('button[title="Remove from saved"]').first();
    await expect(unsaveButton).toBeVisible();
    await unsaveButton.click();

    // Confirm empty state renders
    await expect(page.locator("text=No saved opportunities yet")).toBeVisible({ timeout: 5000 });
  });

  test("Journey C: Multi-User Security and Authorization Isolation", async ({ browser }) => {
    // User A context
    const contextA = await browser.newContext();
    const pageA = await contextA.newPage();

    await pageA.goto("/register");
    await pageA.waitForLoadState("networkidle");
    await pageA.fill('input[autocomplete="name"]', userA.name + " Iso");
    await pageA.fill('input[autocomplete="email"]', `iso-a-${timestamp}@nexus.io`);
    await pageA.fill('input[autocomplete="new-password"]', "Password123!");
    await pageA.click('button[type="submit"]');
    await expect(pageA).toHaveURL(/.*(onboarding|dashboard)/, { timeout: 10000 });

    // User A saves an opportunity
    await pageA.goto("/opportunities");
    await pageA.waitForLoadState("networkidle");
    const inspectBtn = pageA.locator('button:has-text("Inspect")').first();
    await inspectBtn.click();
    await expect(pageA).toHaveURL(/.*opportunities\/[a-zA-Z0-9-]+/, { timeout: 10000 });
    
    // Save as User A
    const saveBtnA = pageA.locator('button:has-text("Save Opportunity")');
    if (await saveBtnA.isVisible()) {
      await saveBtnA.click();
    }

    // User B context (separate cookies/session)
    const contextB = await browser.newContext();
    const pageB = await contextB.newPage();

    await pageB.goto("/register");
    await pageB.waitForLoadState("networkidle");
    await pageB.fill('input[autocomplete="name"]', userB.name + " Iso");
    await pageB.fill('input[autocomplete="email"]', `iso-b-${timestamp}@nexus.io`);
    await pageB.fill('input[autocomplete="new-password"]', "Password123!");
    await pageB.click('button[type="submit"]');
    await expect(pageB).toHaveURL(/.*(onboarding|dashboard)/, { timeout: 10000 });

    // User B checks /saved - must be empty!
    await pageB.goto("/saved");
    await expect(pageB.locator("text=No saved opportunities yet")).toBeVisible();

    await contextA.close();
    await contextB.close();
  });

  test("Journey D: Personalized Recommendations, Match Scoring & AI Intelligence", async ({ page }) => {
    const userRec = {
      name: `Rec User ${timestamp}`,
      email: `rec-${timestamp}@nexus.io`,
      password: "Password123!",
    };

    // 1. Register User
    await page.goto("/register");
    await page.waitForLoadState("networkidle");
    await page.fill('input[autocomplete="name"]', userRec.name);
    await page.fill('input[autocomplete="email"]', userRec.email);
    await page.fill('input[autocomplete="new-password"]', userRec.password);
    await page.click('button[type="submit"]');

    await expect(page).toHaveURL(/.*(onboarding|dashboard)/, { timeout: 10000 });

    // 2. Open Recommendations Feed
    await page.goto("/recommendations");
    await page.waitForLoadState("networkidle");
    await expect(page.locator("main h1")).toContainText(/Personalized Opportunity Intelligence/i);

    // 3. Verify Refresh button exists and recommendations feed loads
    const refreshBtn = page.locator('button:has-text("Recalculate Matches")');
    await expect(refreshBtn).toBeVisible();

    // 4. Navigate to an opportunity details page from recommendation or opportunities
    await page.goto("/opportunities");
    await page.waitForLoadState("networkidle");
    const inspectBtn = page.locator('button:has-text("Inspect")').first();
    await inspectBtn.click();
    await expect(page).toHaveURL(/.*opportunities\/[a-zA-Z0-9-]+/, { timeout: 10000 });

    // 5. Verify Match Intelligence elements and Skill Gap Matrix
    await expect(page.locator("text=Personalized Match Intelligence")).toBeVisible({ timeout: 10000 });
    await expect(page.locator("text=Technical Skill Gap Matrix")).toBeVisible();
    await expect(page.locator("text=NEXUS Intelligence Analysis")).toBeVisible();

    // 6. Test AI Analysis trigger
    const analyzeBtn = page.locator('button:has-text("Synthesize AI Analysis")');
    if (await analyzeBtn.isVisible()) {
      await analyzeBtn.click();
      await expect(page.locator("text=Model Provenance:")).toBeVisible({ timeout: 15000 });
    }
  });
});
