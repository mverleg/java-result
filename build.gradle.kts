plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}

val isEnablePreview = JavaVersion.current() == JavaVersion.VERSION_15 || JavaVersion.current() == JavaVersion.VERSION_16
if (isEnablePreview) {
    // Inspired by https://dzone.com/articles/gradle-goodness-enabling-preview-features-for-java
    tasks {
        val ENABLE_PREVIEW = "--enable-preview"

        withType<JavaCompile>() {
            options.compilerArgs.add(ENABLE_PREVIEW)
            options.release.set(15)
        }

        withType<Test>().all {
            jvmArgs(ENABLE_PREVIEW)
        }

        withType<JavaExec>() {
            jvmArgs(ENABLE_PREVIEW)
        }
    }
}
