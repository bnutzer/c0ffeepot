
plugins {
	java
	checkstyle
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

checkstyle {
    isIgnoreFailures = false
    isShowViolations = true
    maxErrors = 0
    maxWarnings = 0
}

dependencies {

    testImplementation("org.springframework:spring-web:6.1.+")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.+")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.+")

    testImplementation("org.slf4j:slf4j-simple:2.+")

    testImplementation("org.testcontainers:junit-jupiter:1.+")
    testImplementation("org.apache.httpcomponents.client5:httpclient5:5.+")
    testImplementation("org.assertj:assertj-core:3.+")
}
