/**
 * CLI Tests: Tab Context Tracking
 * Verifies that tab-switch and tab-new correctly track the active tab
 * so subsequent commands target the right context.
 */

const { test, describe, before, after } = require('node:test');
const assert = require('node:assert');
const { execSync, spawn } = require('node:child_process');
const path = require('path');
const { VIBIUM } = require('../helpers');

let serverProcess, baseURL;

before(async () => {
  serverProcess = spawn('node', [path.join(__dirname, '../helpers/test-server.js')], {
    stdio: ['pipe', 'pipe', 'pipe'],
  });
  baseURL = await new Promise((resolve) => {
    serverProcess.stdout.once('data', (data) => {
      resolve(data.toString().trim());
    });
  });

  // Navigate tab 0 to the home page
  execSync(`${VIBIUM} go ${baseURL}/`, { encoding: 'utf-8', timeout: 30000 });
});

after(() => {
  // Close any extra tabs created during tests (switch to 1 and close, if it exists)
  try {
    execSync(`${VIBIUM} tab-switch 1`, { encoding: 'utf-8', timeout: 10000 });
    execSync(`${VIBIUM} tab-close`, { encoding: 'utf-8', timeout: 10000 });
  } catch {
    // ignore — tab may not exist
  }
  if (serverProcess) serverProcess.kill();
});

describe('CLI: Tab Context Tracking', () => {
  test('tab-switch targets correct tab for subsequent commands', () => {
    // Tab 0 is already on the home page (title: "The Internet")
    // Create tab 1 and navigate to login page
    execSync(`${VIBIUM} tab-new ${baseURL}/login`, {
      encoding: 'utf-8',
      timeout: 30000,
    });

    // tab-new should have switched to the new tab — verify title
    const loginTitle = execSync(`${VIBIUM} title`, {
      encoding: 'utf-8',
      timeout: 30000,
    }).trim();
    assert.match(loginTitle, /Login/, 'New tab should show login page title');

    // Switch back to tab 0 and verify title
    execSync(`${VIBIUM} tab-switch 0`, { encoding: 'utf-8', timeout: 30000 });
    const homeTitle = execSync(`${VIBIUM} title`, {
      encoding: 'utf-8',
      timeout: 30000,
    }).trim();
    assert.match(homeTitle, /The Internet/, 'Tab 0 should show home page title');

    // Switch to tab 1 and verify title
    execSync(`${VIBIUM} tab-switch 1`, { encoding: 'utf-8', timeout: 30000 });
    const loginTitle2 = execSync(`${VIBIUM} title`, {
      encoding: 'utf-8',
      timeout: 30000,
    }).trim();
    assert.match(loginTitle2, /Login/, 'Tab 1 should show login page title');

    // Cleanup: close tab 1
    execSync(`${VIBIUM} tab-close`, { encoding: 'utf-8', timeout: 30000 });
  });

  test('tab-new switches to the new tab', () => {
    // Tab 0 is on the home page
    const homeTitleBefore = execSync(`${VIBIUM} title`, {
      encoding: 'utf-8',
      timeout: 30000,
    }).trim();
    assert.match(homeTitleBefore, /The Internet/, 'Should start on home page');

    // Open a new tab with the login page
    execSync(`${VIBIUM} tab-new ${baseURL}/login`, {
      encoding: 'utf-8',
      timeout: 30000,
    });

    // Title should now be the login page (we're on the new tab)
    const titleAfter = execSync(`${VIBIUM} title`, {
      encoding: 'utf-8',
      timeout: 30000,
    }).trim();
    assert.match(titleAfter, /Login/, 'Should be on the new tab after tab-new');

    // Cleanup: close the new tab
    execSync(`${VIBIUM} tab-close`, { encoding: 'utf-8', timeout: 30000 });
  });

  test('tab-close without index closes the active tab', () => {
    // Tab 0 is on the home page
    execSync(`${VIBIUM} tab-new ${baseURL}/login`, {
      encoding: 'utf-8',
      timeout: 30000,
    });

    // We're now on tab 1 (login page). Close without index — should close tab 1.
    const result = execSync(`${VIBIUM} tab-close`, {
      encoding: 'utf-8',
      timeout: 30000,
    });
    assert.match(result, /closed/i, 'Should confirm tab closed');

    // Only tab 0 should remain, and it should be the home page
    const title = execSync(`${VIBIUM} title`, {
      encoding: 'utf-8',
      timeout: 30000,
    }).trim();
    assert.match(title, /The Internet/, 'Remaining tab should be home page');

    const tabs = execSync(`${VIBIUM} tabs`, {
      encoding: 'utf-8',
      timeout: 30000,
    });
    // Should only have one tab
    assert.ok(!tabs.includes('[1]'), 'Should only have one tab remaining');
  });
});
