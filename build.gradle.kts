plugins {
    id("java")
    id("application")
    id("maven-publish")
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
version = "1.0.0-SNAPSHOT"

java{
    withJavadocJar()
    withSourcesJar()
}

// publishing
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // include your compiled JAR + sources + javadoc

            // Metadata for the POM
            groupId = project.group.toString()
            artifactId = "jbn-client-server"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/YaminAhmed123/client_server")
            credentials {
                username = System.getenv("GITHUB_USERNAME") ?: "<fallback username>"
                password = System.getenv("GIT_HUB_TOKEN") ?: "<fallback token>"
            }
        }
    }
}


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