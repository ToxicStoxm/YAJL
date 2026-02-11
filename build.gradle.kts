plugins {
    id("java-library")
    alias(libs.plugins.vanniktech.maven.publish)
}

group = "com.toxicstoxm"
version = "2.2.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.jetbrains.annotations)
    annotationProcessor(libs.jetbrains.annotations)

    implementation(libs.yajsi)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.test {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed", "failed", "skipped")
        outputs.upToDateWhen {false}
        showStandardStreams = true
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

    coordinates("com.toxicstoxm", "YAJL", version as String?)

    pom {
        name = "YAJL"
        description = "YAJL (Yet another Java logger) is an easy to use logging framework, with a lot of features."
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
                url = "https://toxicstoxm.com"
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

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Automatic-Module-Name" to "YAJL"
        )
    }
}

tasks.withType<Javadoc>().configureEach {
    isFailOnError = false
}