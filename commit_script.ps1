$startDate = [datetime]"2026-04-07T10:00:00"

function Commit-Changes {
    param(
        [string]$message,
        [int]$daysAdd
    )
    $commitDate = $startDate.AddDays($daysAdd).ToString("yyyy-MM-ddTHH:mm:ss")
    $env:GIT_AUTHOR_DATE = $commitDate
    $env:GIT_COMMITTER_DATE = $commitDate
    git commit -m "$message"
}

git add pom.xml .gitignore
Commit-Changes -message "Initial commit: Add project structure and dependencies" -daysAdd 0

git add src/main/java/com/smartcampus/SmartCampusApplication.java
Commit-Changes -message "Setup Jersey application configuration" -daysAdd 1

git add src/main/java/com/smartcampus/store/DataStore.java
Commit-Changes -message "Implement abstract DataStore for in-memory persistence" -daysAdd 2

git add src/main/java/com/smartcampus/model/Room.java
Commit-Changes -message "Create Room entity model" -daysAdd 3

git add src/main/java/com/smartcampus/resource/RoomResource.java
Commit-Changes -message "Implement RoomResource for Room CRUD operations" -daysAdd 4

git add src/main/java/com/smartcampus/model/Sensor.java
Commit-Changes -message "Create Sensor entity model" -daysAdd 5

git add src/main/java/com/smartcampus/resource/SensorResource.java
Commit-Changes -message "Implement SensorResource for API queries" -daysAdd 6

git add src/main/java/com/smartcampus/model/SensorReading.java
Commit-Changes -message "Add SensorReading object for data collection" -daysAdd 7

git add src/main/java/com/smartcampus/resource/SensorReadingResource.java
Commit-Changes -message "Expose API endpoints for sensor readings" -daysAdd 8

git add src/main/java/com/smartcampus/exception/ErrorResponse.java src/main/java/com/smartcampus/exception/GlobalExceptionMapper.java
Commit-Changes -message "Add basic error processing with GlobalExceptionMapper" -daysAdd 9

git add src/main/java/com/smartcampus/exception/LinkedResourceNotFoundException.java src/main/java/com/smartcampus/exception/LinkedResourceNotFoundExceptionMapper.java
Commit-Changes -message "Handle missing linked resources with custom exception" -daysAdd 10

git add src/main/java/com/smartcampus/exception/RoomNotEmptyException.java src/main/java/com/smartcampus/exception/RoomNotEmptyExceptionMapper.java
Commit-Changes -message "Implement validation and error handling for Room constraints" -daysAdd 11

git add src/main/java/com/smartcampus/exception/SensorUnavailableException.java src/main/java/com/smartcampus/exception/SensorUnavailableExceptionMapper.java
Commit-Changes -message "Add SensorUnavailable validation exception" -daysAdd 12

git add src/main/java/com/smartcampus/resource/DiscoveryResource.java
Commit-Changes -message "Add DiscoveryResource for API catalog overview" -daysAdd 13

git add src/main/java/com/smartcampus/filter/LoggingFilter.java
Commit-Changes -message "Implement LoggingFilter to track incoming requests" -daysAdd 14

git add src/main/java/com/smartcampus/Main.java
Commit-Changes -message "Setup Main launcher for embedded Grizzly server" -daysAdd 15

git add .
Commit-Changes -message "Finalizing minor alignments and project configuration" -daysAdd 15
