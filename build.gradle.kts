plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "com.toxicstoxm"
version = "2.0.6"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("org.jetbrains:annotations:26.0.2-1")
    annotationProcessor("org.jetbrains:annotations:26.0.2-1")

    implementation("org.yaml:snakeyaml:2.4")

    implementation("com.toxicstoxm.YAJSI:YAJSI:2.1.5")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed", "failed", "skipped")
        outputs.upToDateWhen {false}
        showStandardStreams = true  // <-- This ensures System.out output is shown
    }
}

tasks.jar {
    dependsOn("fatJar")
    manifest {
        attributes ["Main-Class"] = "com.toxicstoxm.YAJL.YAJLLogger"
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("com.toxicstoxm.YAJL", "YAJL", version as String?)

    pom {
        name = "YAJL"
        description = "YAJL (Yet another Java logger) is an easy to use logger that can take your project to the next level."
        inceptionYear = "2024"
        url = "https://github.com/ToxicStoxm/YAJL/"
        licenses {
            license {
                name = "The GNU General Public License, Version 3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.html"
                distribution = "https://www.gnu.org/licenses/gpl-3.0.html"
            }
        }
        developers {
            developer {
                id = "toxicstoxm"
                name = "ToxicStoxm"
                url = "https://github.com/ToxicStoxm/"
            }
        }
        scm {
            url = "https://github.com/ToxicStoxm/YAJL/"
            connection = "scm:git:git://github.com/ToxicStoxm/YAJL.git"
            developerConnection = "scm:git:ssh://git@github.com/ToxicStoxm/YAJL.git"
        }
    }
}

tasks.register<Jar>("fatJar") {
    manifest {
        attributes["Main-Class"] = "com.toxicstoxm.YAJL.YAJLLogger"
    }

    archiveBaseName = "${rootProject.name}-fat"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    from(configurations.compileClasspath.get().filter { it.isDirectory || it.isFile }.map {
        if (it.isDirectory) it else zipTree(it)
    })
    with(tasks.jar.get())
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
}