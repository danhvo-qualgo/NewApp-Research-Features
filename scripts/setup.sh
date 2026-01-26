#!/bin/bash

set -e

# Basic logging functions
log_info() { echo "[INFO] $1"; }
log_success() { echo "[SUCCESS] $1"; }
log_error() { echo "[ERROR] $1"; }

# Copy config examples to config folder
log_info "Copying configuration examples..."

if [[ -d "scripts/config-example" ]]; then
    mkdir -p config
    cp -r scripts/config-example/* config/
    log_success "✓ Configuration examples copied to config/"
else
    log_error "scripts/config-example directory not found"
    exit 1
fi

# Install Ruby dependencies
log_info "Installing Ruby dependencies..."
bundle install
log_success "✓ Ruby dependencies installed"

# Fetch version using Fastlane
log_info "Fetching version..."
bundle exec fastlane fetch_version
log_success "✓ Version fetched"