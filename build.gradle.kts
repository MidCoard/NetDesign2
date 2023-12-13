import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("app.cash.sqldelight") version "2.0.0"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    implementation("top.focess:focess-util:1.1.17")
    implementation("top.focess:focess-scheduler:1.2.0")
    implementation("com.google.protobuf:protobuf-java:3.25.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.0")
    implementation("app.cash.sqldelight:sqlite-driver:2.0.0")

    // Include the Test API
    testImplementation(compose.desktop.uiTestJUnit4)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("top.focess.netdesign")
        }
    }
}

compose.desktop {
    application {
        mainClass = "top.focess.netdesign.ui.NetDesign2Kt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NetDesign2"
            packageVersion = "1.0.0"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))
        }
    }
}
