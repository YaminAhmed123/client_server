plugins {
    id("java")
    id("application")
}

application {
    mainClass = "io.github.yaminahmed123.Main"
}

// executable jar configuration
tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}


group = "io.github.yaminahmed123"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}