# App Setup Guide

This guide walks you through setting up the App project for development.

## Prerequisites

- **Ruby** (for fastlane and dependencies)
- **Android Studio** (for Android development)
- **Git** (for version control)

## Setup Steps

### 1. Install Dependencies

Install Ruby gems required for the project:

```bash
bundle install
```

This will install:

- Fastlane (for automation)
- Required gems for version catalog management
- Development dependencies

### 2. Initialize Project Configuration

Run the setup script to configure your development environment:

```bash
sh scripts/setup.sh
```

This script will:

- Run `init-project.sh` if it exists
- Copy configuration files from `scripts/misc-example/` to `misc/`
- Set up necessary project structure

### 3. Fetch Latest Versions

Fetch the latest dependency versions from the version repository:

```bash
bundle exec fastlane fetch_latest_version
```

This command will:

- Clone the version catalog repository
- Merge `thirdparties.versions.toml` and `libraries.versions.toml`
- Generate `gradle/libs.versions.toml` with the latest dependency versions
- Report any conflicts between version files

## Configuration Files

After running the setup, you'll have these configuration files in the `misc/` directory:

- **Config files**: Environment-specific configuration files
- **Signing files**: Certificates and signing configurations

## Troubleshooting

### Common Issues

**Bundle install fails:**

```bash
gem install bundler
bundle install
```

**Fastlane version fetch fails:**

- Check your internet connection
- Verify `VERSION_CATALOG_REPO` and `VERSION_CATALOG_BRANCH` in `gradle.properties`
- Ensure the repository is accessible

## Next Steps

After successful setup:

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project
4. Run tests to verify everything works

## Version Management

The project uses a centralized version catalog system:

- Version definitions are stored in a separate repository
- `fastlane fetch_latest_version` syncs the latest versions
- Dependencies are managed through `gradle/libs.versions.toml`
