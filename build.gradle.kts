plugins {
    java
}

group = "com.sandeep.concurrency"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.20.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.2")
}