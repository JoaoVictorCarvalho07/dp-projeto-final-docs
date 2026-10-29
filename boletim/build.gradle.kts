plugins { application }

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

dependencies {
    implementation(project(":lib"))
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }

application { mainClass = "br.pucpr.boletim.DemoPadrao" }

// roda a demo com as extensoes: ./gradlew :boletim:runExtensao
tasks.register<JavaExec>("runExtensao") {
    group = "application"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass = "br.pucpr.boletim.DemoExtensao"
    workingDir = projectDir
}

tasks.named<JavaExec>("run") { workingDir = projectDir }
