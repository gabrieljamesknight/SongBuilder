# SongBuilder

SongBuilder is a desktop application designed specifically for guitarists and songwriters to compose, arrange, and manage their musical ideas. Built with Java, it provides an intuitive interface for synchronizing lyrics, chord charts, and tablature into a cohesive workflow.

## Features

* Intuitive Song Structuring: Arrange songs by standard sections (Verse, Chorus, Bridge) using a dedicated UI.
* Guitar-Centric Notation: Dedicated input handling for standard guitar tablature and chord symbols.
* Precision Alignment: Ensures chords and tablature notations remain perfectly aligned with lyrics across different screen    resolutions.
* State Persistence: Save and load song projects using robust JSON serialization for easy sharing and backup.
* Modular UI: Built using custom components to ensure a responsive and maintainable user experience.

## Architecture

This project is structured with an emphasis on maintainability and scalability, adhering to SOLID principles and standard design patterns:

* Separation of Concerns: The application strictly separates the graphical user interface (GUI) from the underlying business logic and data models.
* Component-Based UI: Complex views are broken down into smaller, reusable Java UI components.
* Build Automation: Managed via Apache Maven for consistent dependency resolution and reliable builds.

## Prerequisites

To build and run SongBuilder locally, ensure you have the following installed:

* Java Development Kit (JDK) 11 or higher
* Apache Maven 3.6+
* Git

## Installation and Build

1. Clone the repository:
   git clone https://github.com/gabrieljamesknight/SongBuilder.git

2. Navigate to the project directory:
   cd SongBuilder

3. Build the project using Maven:
   mvn clean install

4. Run the application:
   mvn exec:java -Dexec.mainClass="main.SongBuilderApp"

## Usage

Upon launching SongBuilder, create a new project from the File menu. You can add new song lines, input lyrics into the primary text fields, and use the dedicated chord/tab notation fields above them. Use the standard save dialog to export your work as a structured JSON file.

## License

This project is licensed under the MIT License - see the LICENSE file for details.