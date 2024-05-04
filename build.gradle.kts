plugins {
    java
    `maven-publish`
}

group = "dev.xdark"
version = "2.1.0"

repositories.mavenCentral()

dependencies {
    val junitVersion = "5.10.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

    val asmVersion = "9.7"
    testImplementation("org.ow2.asm:asm:$asmVersion")
    testImplementation("org.ow2.asm:asm-tree:$asmVersion")

    val annotationsVersion = "24.1.0"
    compileOnly("org.jetbrains:annotations:$annotationsVersion")
    testCompileOnly("org.jetbrains:annotations:$annotationsVersion")
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
