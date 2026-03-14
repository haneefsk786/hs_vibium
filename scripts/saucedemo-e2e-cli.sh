#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd "$(dirname "$0")" && pwd)"
out="${script_dir}/../saucedemo-record-cli.zip"

🤖() { "${script_dir}/../clicker/bin/vibium" "$@"; }

# Start daemon (visible browser, background process)
🤖 daemon start
trap '🤖 daemon stop 2>/dev/null || true' EXIT

# Set viewport
🤖 viewport 1280 720

# Start recording with screenshots
🤖 record start --screenshots --name "saucedemo-e2e" --title "SauceDemo E2E Test" --format jpeg --quality 0.1

# 1. Logging in
🤖 record group start "Logging in"
🤖 go "https://www.saucedemo.com"
🤖 fill "#user-name" "standard_user"
🤖 fill "#password" "secret_sauce"
🤖 click "#login-button"
🤖 sleep 500
🤖 record group stop

# 2. Selecting products
🤖 record group start "Selecting products"
🤖 click "#add-to-cart-sauce-labs-backpack"
🤖 click "#add-to-cart-sauce-labs-bike-light"
🤖 click "#add-to-cart-sauce-labs-onesie"
badge=$(🤖 text ".shopping_cart_badge")
if [ "$badge" != "3" ]; then
    echo "FAIL: Expected cart badge '3', got '$badge'" >&2
    exit 1
fi
echo "Cart badge: $badge"
🤖 record group stop

# 3. Reviewing cart
🤖 record group start "Reviewing cart"
🤖 click ".shopping_cart_link"
🤖 sleep 300
🤖 click "#remove-sauce-labs-bike-light"
🤖 record group stop

# 4. Checking out
🤖 record group start "Checking out"
🤖 click "#checkout"
🤖 fill "#first-name" "Test"
🤖 fill "#last-name" "User"
🤖 fill "#postal-code" "90210"
🤖 click "#continue"
🤖 sleep 300
🤖 record group stop

# 5. Completing order
🤖 record group start "Completing order"
🤖 click "#finish"
🤖 sleep 500
confirmation=$(🤖 text ".complete-header")
if [[ "$confirmation" != *"Thank you"* ]]; then
    echo "FAIL: Unexpected confirmation: '$confirmation'" >&2
    exit 1
fi
echo "Confirmation: $confirmation"
🤖 record group stop

# 6. Logging out
🤖 record group start "Logging out"
🤖 click "#react-burger-menu-btn"
🤖 sleep 400
🤖 click "#logout_sidebar_link"
🤖 sleep 300
loginBtn=$(🤖 text "#login-button")
echo "Back on login page: $loginBtn"
🤖 record group stop

# Stop recording & save
🤖 record stop -o "$out"
echo "Recording saved → $out"
