plugins {
    java
    `maven-publish`
}

group = "dev.xdark"
version = "3.0.0"

repositories.mavenCentral()

dependencies {
    testImplementation(libs.bundles.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.bundles.asm)

    compileOnly(libs.jetbrains.annotations)
    testCompileOnly(libs.jetbrains.annotations)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("-parameters", "-g:lines,source,vars"))
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
        pom {
            name = "jlinker"
            description = "Java member resolution library"
        }

        from(components["java"])
    }
}
