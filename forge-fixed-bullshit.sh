#!/bin/bash

echo "Killing the Gradle daemon to free up memory locks..."
./gradlew --stop 2>/dev/null || gradle --stop 2>/dev/null

echo -e "\nHunting down exact matches for '1-snapshot-1' in global caches..."
# -r (recursive), -l (print only file names), -I (ignore binary), -w (exact word match)
grep -rlIw "1-snapshot-1" ~/.gradle/caches/ 2>/dev/null

echo -e "\nHunting down exact matches in project caches..."
grep -rlIw "1-snapshot-1" .gradle/ .loom/ build/ 2>/dev/null

echo -e "\nHunting down exact matches in project configuration..."
grep -rlIw "1-snapshot-1" build.gradle settings.gradle gradle.properties buildSrc/ 2>/dev/null

echo -e "\nSearch complete."