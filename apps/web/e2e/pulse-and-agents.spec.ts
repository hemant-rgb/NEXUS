import { test, expect } from "@playwright/test";

test.describe("NEXUS Pulse & Autonomous Agents (Phases 8 & 9)", () => {

  const createAuthenticatedUser = async (page: any) => {
    const unique = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
    const email = `agent.user.${unique}@nexus.dev`;
    const password = "Password123!";

    await page.goto("/register");
    await page.waitForLoadState("networkidle");
    await page.fill('input[autocomplete="name"]', `Agent Operator ${unique}`);
    await page.fill('input[autocomplete="email"]', email);
    await page.fill('input[autocomplete="new-password"]', password);
    await page.click('button[type="submit"]');

    // Wait for redirect to onboarding
    await expect(page).toHaveURL(/.*onboarding/, { timeout: 10000 });
  };

  test("Journey H: NEXUS Pulse — Alerts, Monitoring Scanner & Preferences", async ({ page }) => {
    await createAuthenticatedUser(page);

    // 1. Navigate to Pulse Activity
    await page.goto("/activity");
    await page.waitForLoadState("networkidle");

    // Check Header uniquely
    await expect(page.getByRole("heading", { name: "NEXUS Pulse", exact: true })).toBeVisible({ timeout: 10000 });

    // 2. Trigger Pulse Monitoring Scan
    const scanBtn = page.locator('button:has-text("Scan Monitoring")');
    await expect(scanBtn).toBeVisible();
    await scanBtn.click();

    // Verify scan feedback banner appears
    await expect(page.locator("text=Pulse cycle completed")).toBeVisible({ timeout: 10000 });

    // 3. Switch to Notification Preferences Tab
    const prefsTab = page.locator('button:has-text("Notification Preferences")');
    await prefsTab.click();

    // Verify preferences form loaded
    await expect(page.locator("text=Delivery Channels")).toBeVisible();
    await expect(page.locator("text=Quiet Hours & Timezone")).toBeVisible();

    // Toggle email notifications
    const emailCheckbox = page.locator('input[type="checkbox"]').nth(1);
    await emailCheckbox.click();

    // Save preferences
    const saveBtn = page.locator('button:has-text("Save Pulse Preferences")');
    await saveBtn.click();
    await expect(page.locator("text=Notification preferences updated successfully")).toBeVisible({ timeout: 5000 });
  });

  test("Journey I: NEXUS Autonomous Agents — Roster, Deployment, Step Trace & Level 3 Approval", async ({ page }) => {
    await createAuthenticatedUser(page);

    // 1. Navigate to Autonomous Agent Workspace
    await page.goto("/agents");
    await page.waitForLoadState("networkidle");

    // Check Header and Engine Badge uniquely
    await expect(page.getByRole("heading", { name: "NEXUS Autonomous Agents", exact: true })).toBeVisible({ timeout: 10000 });
    await expect(page.locator("text=Phase 9 Engine")).toBeVisible();

    // 2. Verify Specialized Agents Roster loads
    await expect(page.locator("text=NEXUS Scout Agent").first()).toBeVisible({ timeout: 10000 });
    await expect(page.locator("text=NEXUS Research Agent").first()).toBeVisible();
    await expect(page.locator("text=NEXUS Strategy Agent").first()).toBeVisible();
    await expect(page.locator("text=NEXUS GitHub Agent").first()).toBeVisible();
    await expect(page.locator("text=NEXUS Application Agent").first()).toBeVisible();
    await expect(page.locator("text=NEXUS Monitoring Agent").first()).toBeVisible();

    // 3. Deploy Scout Agent
    await page.locator("text=NEXUS Scout Agent").first().click();
    const goalInput = page.locator('textarea[required]');
    await goalInput.fill("Discover top matching backend engineering internships");
    const deployBtn = page.locator('button:has-text("Deploy SCOUT")');
    await deployBtn.click();

    // 4. Verify Execution Trace and Step Telemetry
    await expect(page.locator("text=deployed successfully")).toBeVisible({ timeout: 8000 });
    await expect(page.locator("text=Execution Trace & Steps")).toBeVisible();
    await expect(page.locator("text=COMPLETED").first()).toBeVisible({ timeout: 5000 });
    await expect(page.locator("text=Executive Summary:")).toBeVisible();

    // 5. Test Level 3 Safety Approval Gating with Application Agent
    await page.locator("text=NEXUS Application Agent").first().click();
    await goalInput.fill("Prepare tailored application response and request submission");
    
    // Check external submission checkbox
    const submitCheckbox = page.locator('input[type="checkbox"]').first();
    await submitCheckbox.click();

    const deployAppBtn = page.locator('button:has-text("Deploy APPLICATION")');
    await deployAppBtn.click();

    // Verify Level 3 Safety Pause Banner appears
    await expect(page.locator("text=Level 3 Safety Pause: Authorization Required")).toBeVisible({ timeout: 8000 });

    // Authorize action
    const authorizeBtn = page.locator('button:has-text("Authorize Action")');
    await expect(authorizeBtn).toBeVisible();
    await authorizeBtn.click();

    // Verify authorized action resumes to COMPLETED
    await expect(page.locator("text=Autonomous action authorized and resumed")).toBeVisible({ timeout: 8000 });
  });
});
