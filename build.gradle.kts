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
    implementation("top.focess:focess-scheduler:1.2.1")
    implementation("com.google.protobuf:protobuf-java:3.25.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.25.0")
    implementation("app.cash.sqldelight:sqlite-driver:2.0.0")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("uk.org.lidalia:sysout-over-slf4j:1.0.2")

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

        val osConfigDir: String = when {
            System.getProperty("os.name").startsWith("Windows") -> System.getenv("APPDATA")
            System.getProperty("os.name").startsWith("Mac") -> System.getProperty("user.home") + "/Library/Application Support"
            else -> System.getProperty("user.home") + "/.config" // Assume Linux
        }

        val configDir = "$osConfigDir${File.separator}NetDesign2"

        jvmArgs += listOf("-Xmx4G", "-DNET_DESIGN_2_LOG_PATH=$configDir${File.separator}logs")

//        args += listOf("--local")


        nativeDistributions {
            buildTypes {
                release {
                    proguard {
                        configurationFiles.from(project.file("proguard-rules.pro"))
                    }
                }
            }
            includeAllModules = true

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NetDesign2"
            packageVersion = "1.0.0"
            version = "1.0.0"
            description = "NetDesign2"
            copyright = "© 2023 Focess. All rights reserved."
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))

            windows {
                console = true
                dirChooser = true
                perUserInstall = true
                menuGroup = "NetDesign2"
                upgradeUuid = "9edd3750-f718-4169-b33b-00284d77de93"
            }
        }
    }
}
