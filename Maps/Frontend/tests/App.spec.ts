import { test, expect } from "@playwright/test";

/**
 * This is carried out before every test that is run. It is responsible for
 * loading the page. By doing this in the beforeEach, we reduce redundency.
 */
test.beforeEach(async ({ page }, testInfo) => {
  await page.goto("http://localhost:5173/");
});

/**
 * This tests that the command input bar is visible on the page.
 */
test("input bar appears", async ({ page }) => {
  await expect(page.getByLabel("Command input")).toBeVisible();
});

/**
 * This tests that the history box is visible on the page.
 */
test("history box appears", async ({ page }) => {
  await expect(page.getByLabel("Past commands")).toBeVisible();
});

/**
 * This tests that the text in the command input bar changes after typing in
 * a some input.
 */
test("command input text changes", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("Awesome command");
  const mock_input = `Awesome command`;
  await expect(page.getByLabel("Command input")).toHaveValue(mock_input);
});

/**
 * Tests basic broadband querying.
 */
test("broadband", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page
    .getByLabel("Command input")
    .fill("broadband <Rhode Island> <Providence County>");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Broadband percentage for Providence County, Rhode Island: 85.4"
  );
});

/**
 * Tests that an error message is appropriately displayed when a state and county
 * that do not have census data are inputted by the user.
 */
test("invalid state and county", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("broadband <invalid state> <invalid county>");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Broadband error - check server API connection or ensure provided state and county are valid"
  );
});

/**
 * Tests that an informative and accurate error message is displayed to the user
 * when an invalid number of arguments are inputted after the broadband command.
 */
test("invalid broadband command", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("broadband too many inputs");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Please enter a valid broadband command: broadband <state> <county>"
  );
});

test("highlight area", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("highlight Boston");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Search successful! Look on your map for the highlighted areas!"
  );
});

test("highlight nonexistant area", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("highlight nonexistant");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "No results for your area description, please try another one and make sure your format is: highlight <area description>"
  );
});

test("invalid highlight command", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("highlight");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "No results for your area description, please try another one and make sure your format is: highlight <area description>"
  );
});

test("invalid command", async ({ page }) => {
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("this is not valid");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Please enter a valid command (broadband <state> <county> or highlight <area description>)"
  );
});

/**
 */
test("broadband and highlight interaction", async ({ page }) => {
  await page.goto("http://localhost:5173/");

  //invalid command
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("this is not valid");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Please enter a valid command (broadband <state> <county> or highlight <area description>)"
  );

  // successful area key word search
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("highlight Excellent");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Search successful! Look on your map for the highlighted areas!"
  );

  // invalid broadband state and county
  await page.getByLabel("Command input").click();
  await page
    .getByLabel("Command input")
    .fill("broadband <invalid state> <invalid county>");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Broadband error - check server API connection or ensure provided state and county are valid"
  );

  // successful broadband search
  await page.getByLabel("Command input").click();
  await page
    .getByLabel("Command input")
    .fill("broadband <California> <Kings County>");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Broadband percentage for Kings County, California: 83.5"
  );

  //invalid highlight command
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("highlight");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "No results for your area description, please try another one and make sure your format is: highlight <area description>"
  );

  // no area key word results
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("highlight hdjshf");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "No results for your area description, please try another one and make sure your format is: highlight <area description>"
  );

  // invalid broadband command
  await page.getByLabel("Command input").click();
  await page.getByLabel("Command input").fill("broadband");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  await expect(page.getByLabel("Past commands")).toContainText(
    "Please enter a valid broadband command: broadband <state> <county>"
  );
});
