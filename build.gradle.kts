
plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
