# WatchBack Android

## Table of Contents

1. [Development and branching information](#development-and-branching-information)
2. [Versioning WatchBack Android builds](#versioning-watchback-android-builds)
3. [Tagging WatchBack Android builds](#tagging-watchback-android-builds)
4. [Adding Release Notes](#adding-release-notes)

## Development and branching information

Currently we have the following branching struture followed:

1. **master**
2. **release/[version]**
3. **develop**

For development purposes, we will utilize just the `develop` branch. For every feature OR bug-fixes being added, a new branch is to be created from `develop` such as `feature/home_screen_implementation` OR `bugfix/blank_title_fix` and once the necessary implementation is in place, we need to open a Pull-Request for merging our changes to the `develop` branch.

Once the app is released to Play Store, then we merge the `develop` branch to `master` as well as to corresponding `release/[version]` (example: `release/1.0.0`) branch.

The `release/*` branch is also used in case we want to make a hotfix on priority to any issues seen with PlayStore released builds. In this case, we will create a `hotfix/*` branch from corresponding `release/*` branch and merge it back to that `release/*` branch once it is ready. Also, this change would be merged to `develop` branch as well. So that it gets included for the next releases too.

Any git commit pushed to GitHub on the `develop` OR `feature/*` OR `bugfix/*` branches, will notify CircleCI to create a WatchBack Android build. But these builds will be pushed to Fabric only if commit is tagged with version number. As explained in [Tagging WatchBack Android builds](#tagging-watchback-android-builds), please use those tagging rules whenever a build is to be released. 

## Versioning WatchBack Android builds

For internal releases, WatchBack Android builds should be versioned in 4 digits. For e.g. `1.0.0.RC.1`

1. **Major version** - Current major version is `1`
2. **Minor version** - Next version that will be released to Google Play Store. For e.g., `0` in above example
3. **Hot fix version** - Generally this version is `0`. But for some reason any major bug is released in Google Play Store build and we need to release a hot fix, then we bump this version. In above e.g., it is `0`.
4. **RC** - RC indicates that the build is a release candidate for Play Store.
4. **Build number** - Version of the build that has been released internally. With every major, minor, or hot fix version increase, this number should be reset to 0. And then should be incremented by 1 whenever a build is released to QE people for testing. In above e.g., this number is `1`.

Whenever we release a build to QE, we should increase the version number of the build in the `build.gradle` file. 

For releasing builds to PlayStore, the versioning is done in 3 digits. This follows same patters as above, but we omit the `RC` and internal build number in this case.

This version needs to be updated in the `build.gradle` and committed before tagging a build for PlayStore release. Whenever we release a build to Google Play Store, we should increment the version code also in `build.gradle`.

## Tagging WatchBack Android builds

Every WatchBack Android build that is to be released for QA, should be tagged with the name as version number of the build. For e.g., `1.0.0.RC.0` This uploads builds automatically to Fabric/Beta from `CircleCI`. 

Additionally, we need to include a message when tagging so that the correct environment(DEV/PROD) is accordingly selected for building. The environment should be mentioned somewhere in the message and should follow the format `Environment:DEV|PROD` (Format is not case-sensitive)

For .e.g, for tagging `1.1.1.RC.2` for DEV environment, the command would be: `git tag -a 1.1.1.RC.2 -m “Tagging version for build Environment:Dev”`.

**Note: If Environment is not mentioned when tagging, then by default the build would be targeted for PROD environment**

## Adding Release Notes

Currently we maintain all of our release notes in `CHANGELOG.md`. They follow a strict pattern:

1. Heading of the note is version number of the Android build. For e.g., `v1.1.0.RC.2`
2. There is a mandatory sub section `NOTES:`. This section has following details:
   1. The build enviornment being targetted i.e. either `Enviornment: DEV` OR `Enviornment: PROD`
   2. Numeric version code of the build. For e.g. `236`. This code is incremented after a build is released to PlayStore
   3. Tag that is used for tagging build. This should be the version number of the build. For e.g., `1.1.0.RC.2`
3. Optional sub section `New Features`. This section should include the release notes about the new features added to the build. Generally it is recommended to add JIRA task link as well for the feature.
4. Optional sub section `Enhancements`. This section should include the release notes about the enhancements or SDK updates added to the build. Generally it is recommended to add JIRA task link as well for the enhancements..
5. Optional sub section `Bugs Fixed`. This section should include the release notes about the bug fixes added to the build. Generally it is recommended to add JIRA task link as well for the bug fixes.

*NOTE: We need above strict pattern because when we build using `CircleCI`, then we rely on this strict versioning pattern to grab the notes for the build from `CHANGELOG.md`. Otherwise, CircleCI builds can misbehave while grabbing notes for the build.*

