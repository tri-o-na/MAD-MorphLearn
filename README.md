# MorphLearn — Tailored to Your Learning Style
> We morph your learn

MorphLearn is an AI-driven Android application that optimizes study habits by personalizing content to a student's natural learning style. After a short onboarding quiz that identifies whether you learn best visually, auditorily, through reading/writing, or kinesthetically, the app reformats your uploaded school notes and generates personalized quiz questions to improve retention. Built for students who deal with large volumes of academic material and want a more engaging alternative to traditional one-size-fits-all revision tools.

## Tech Stack
* Kotlin + Jetpack Compose
* Firebase Authentication & Firestore
* Android Studio

## Prerequisites
Before running the project, make sure you have the following installed:
* Android Studio Hedgehog (2023.1.1) or later
* JDK 11 or higher
* Android SDK with minimum API level 26 (Android 8.0)
* An Android emulator or physical device configured in Android Studio

## Firebase Setup
This project uses Firebase for authentication and data storage. The google-services.json configuration file is not included in this repository for security reasons.
To run the app, you will need to:
* Contact the team to obtain access to the firebase by sending your gmail to 2402695@sit.singaporetech.edu.sg with the subject line "MorphLearn Firebase Request Access"z

## Continuous Integration
MorphLearn uses a GitHub Actions workflow (`.github/workflows/android.yml`) to automate testing on every push to the `main` and `dev` branches, as well as on every pull request to `main`. The workflow runs on `ubuntu-latest` and performs the following steps:

1. Checks out the repository
2. Sets up JDK 17 (Temurin distribution) with Gradle caching enabled to speed up builds
3. Grants execute permission to the Gradle wrapper
4. Runs all unit tests using `./gradlew testDebugUnitTest`, with the `GEMINI_API_KEY` 
   injected securely from GitHub Secrets so AI-dependent code can be tested without 
   exposing credentials
5. If any tests fail, uploads the test reports as a GitHub Actions artifact so the team 
   can inspect exactly which tests failed directly from the GitHub UI

This ensures that any new changes do not break existing functionality before being merged into the main branch.

## Getting Started
* Clone or import the project into Android Studio via File → New → Import Project from Version Control
* Paste the google-services.json file into the app/ directory
* Let Gradle sync complete (this may take a few minutes on first run)
* Open Device Manager and create an emulator if you don't have one set up — a Pixel 6 with API 33 or above is recommended
* Select your device from the dropdown at the top of Android Studio
* Click the Run button (▶) to build and launch the app

## Features
* Onboarding quiz to detect your VARK learning style (Visual, Auditory, Read/Write, Kinesthetic)
* AI-generated notes and quiz questions tailored to your learning style
* Flashcard mode for visual learners
* Progress dashboard with subject mastery radar chart and topic trend line chart
* Daily streak tracking per subject
