#!/usr/bin/env bash
# Compiles and runs the Library Management System.
set -e
cd "$(dirname "$0")"

echo "Compiling..."
mkdir -p bin
javac -d bin src/*.java

echo "Starting Library Management System..."
java -cp bin LibraryManagementSystem
