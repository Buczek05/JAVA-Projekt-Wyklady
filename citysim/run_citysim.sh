#!/bin/bash

# CitySim Game Launcher Script

# Navigate to the script's directory
cd "$(dirname "$0")"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in the PATH."
    echo "Please install Java 21 or later and try again."
    exit 1
fi

# Check if the JAR file exists
if [ ! -f "target/citysim-fat.jar" ]; then
    echo "Error: Game JAR file not found."
    echo "Building the game..."
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        echo "Error: Maven is not installed or not in the PATH."
        echo "Please install Maven and try again, or build the project manually."
        exit 1
    fi
    
    # Build the project
    mvn clean package
    
    # Check if build was successful
    if [ ! -f "target/citysim-fat.jar" ]; then
        echo "Error: Failed to build the game."
        exit 1
    fi
fi

echo "Starting CitySim game..."
java -jar target/citysim-fat.jar

# Exit with the same status as the Java program
exit $?