fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android fetch_version

```sh
[bundle exec] fastlane android fetch_version
```

Fetch latest version

### android publish

```sh
[bundle exec] fastlane android publish
```

Publish release with auto-bump and remote/local options

### android publish_snapshot

```sh
[bundle exec] fastlane android publish_snapshot
```

Publish snapshot for testing with branch name and timestamp

### android build

```sh
[bundle exec] fastlane android build
```

Build app

### android build_upload_firebase

```sh
[bundle exec] fastlane android build_upload_firebase
```

Build and upload to Firebase App Distribution

### android build_upload_play_store

```sh
[bundle exec] fastlane android build_upload_play_store
```

Build and upload to Google Play Store

### android bump_version

```sh
[bundle exec] fastlane android bump_version
```

Bump app version

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
