#!/bin/bash

set -e

REPO_URL="https://github.com/uneycom/NewApp-Scripts"
TEMP_DIR=$(mktemp -d)
CURRENT_DIR="$(pwd)"

echo "🔄 Fetching new scripts from $REPO_URL..."

# Clone repository to temporary directory
echo "📁 Cloning to temporary directory: $TEMP_DIR"
git clone "$REPO_URL" "$TEMP_DIR"

# Check if fastlane folder exists in the cloned repo
if [ ! -d "$TEMP_DIR/fastlane" ]; then
    echo "❌ Error: fastlane folder not found in the repository"
    rm -rf "$TEMP_DIR"
    exit 1
fi

# Check if fastlane folder exists in current project
if [ -d "$CURRENT_DIR/fastlane" ]; then
    echo "🗑️  Removing existing fastlane folder"
    rm -rf "$CURRENT_DIR/fastlane"
fi

# Copy fastlane folder to current project
echo "📋 Copying fastlane folder to project"
cp -r "$TEMP_DIR/fastlane" "$CURRENT_DIR/"

# Clean up temporary directory
echo "🧹 Cleaning up temporary directory"
rm -rf "$TEMP_DIR"

echo "✅ Successfully updated fastlane folder!"