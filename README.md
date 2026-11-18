# Review Cycle — cloud-buildable Android project

This is a native Android app. It stores review data locally on the device in SQLite.

## Build on Codemagic (no Android Studio installation on your laptop)

1. Extract this ZIP.
2. Create a new repository on GitHub.
3. Upload the **contents of this folder** to the repository root. The `codemagic.yaml` file must be at the repository root.
4. In Codemagic, add the application and connect the GitHub repository.
5. Select the `review-cycle-android` workflow and start a build.
6. When the build succeeds, download `app-debug.apk` from the build artifacts and install it on your Android phone.

The Codemagic workflow uses a macOS M2 build machine, downloads Gradle 8.9, installs Android SDK Platform 35 / Build Tools 35.0.0, and runs `assembleDebug`. This avoids needing the Gradle Wrapper files in this project.

## Codemagic plan note

Codemagic's current pricing documentation lists 500 free build minutes per month for individual accounts on macOS M2. Free minutes are not available to Teams, and Linux/Windows build machines are not included in that free-minute allowance. Check your account's current plan and billing page before starting a build.

## App functionality

- Create subjects and add topics under each subject.
- Home screen lists due and overdue topics.
- Topics inside a subject are ordered by earliest next-review date.
- Five review stages: 3, 5, 7, 15 and 30 days.
- Late reviews calculate the next interval from the actual day you mark the review complete.
- After the fifth review, the topic moves to Completed.
- Settings shows approximate database storage and offers a confirmed delete-all-data action.
- Data is stored locally on the device; there is no account, server, or cloud sync.

## Build output

The workflow creates an unsigned **debug APK** at:
`app/build/outputs/apk/debug/app-debug.apk`

This is suitable for installing and testing on your own phone. A signed release build would require a separate signing-key setup.
