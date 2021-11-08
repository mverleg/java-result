
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


// Inspired by https://dzone.com/articles/gradle-goodness-enabling-preview-features-for-java
tasks {
    val ENABLE_PREVIEW = "--enable-preview"

    // In our project we have the tasks compileJava and
    // compileTestJava that need to have the
    // --enable-preview compiler arguments.
    withType<JavaCompile>() {
        options.compilerArgs.add(ENABLE_PREVIEW)

        // Explicitly setting compiler option --release
        // is needed when we wouldn't set the
        // sourceCompatiblity and targetCompatibility
        // properties of the Java plugin extension.
        options.release.set(15)
    }

    // Test tasks need to have the JVM argument --enable-preview.
    withType<Test>().all {
        jvmArgs(ENABLE_PREVIEW)
    }

    // JavaExec tasks need to have the JVM argument --enable-preview.
    withType<JavaExec>() {
        jvmArgs(ENABLE_PREVIEW)
    }
}
