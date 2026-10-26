plugins { application }

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

dependencies { implementation(project(":lib")) }

application { mainClass = "br.pucpr.example.Main" }
