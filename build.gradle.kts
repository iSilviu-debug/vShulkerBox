plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "it.isilviu"
version = "1.3-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("de.tr7zw:item-nbt-api:2.13.2")
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Required for all platforms
    implementation("com.github.Revxrsal.Lamp:common:3.2.1")

    // Add your specific platform module here
    implementation("com.github.Revxrsal.Lamp:bukkit:3.2.1")
}

tasks.withType<JavaCompile> { // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks {
    shadowJar {
        relocate("de.tr7zw.changeme.nbtapi", "it.isilviu.vshulkerbox.lib.nbtapi")
        relocate("revxrsal", "it.isilviu.vshulkerbox.lib.revxrsal")
    }

    build {
        dependsOn(shadowJar)
    }
}


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.test {
    useJUnitPlatform()
}