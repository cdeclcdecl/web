plugins {
    java
    war
    jacoco
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.flywaydb.flyway") version "9.22.3"
}

group = "ru.msu"
version = "0.0.1-SNAPSHOT"
val jacocoVersion = "0.8.11"
val jacocoRuntimeAgent by configurations.creating

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper")
    implementation("jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api:3.0.0")
    runtimeOnly("org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.20.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add(jacocoRuntimeAgent.name, "org.jacoco:org.jacoco.agent:$jacocoVersion:runtime")
}

jacoco {
    toolVersion = jacocoVersion
}

sourceSets {
    main {
        resources {
            srcDir("src/main/webapp")
        }
    }
    test {
        resources {
            srcDir("db_scripts")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    environment("DOCKER_HOST", "unix:///var/run/docker.sock")
    environment("DOCKER_API_VERSION", "1.43")
    jvmArgs("-Dapi.version=1.43")
}

tasks.test {
    extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class) {
        destinationFile = layout.buildDirectory.file("jacoco/test.exec").get().asFile
    }
    filter {
        excludeTestsMatching("ru.msu.web.system.*")
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("systemTest") {
    group = "verification"
    description = "Запуск системных тестов (требует geckodriver и Firefox)"
    dependsOn(tasks.named("bootWar"))
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("ru.msu.web.system.*")
    }
    systemProperty("systemTest.jacocoAppExecFile", layout.buildDirectory.file("jacoco/systemTest-app.exec").get().asFile.absolutePath)
    extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class) {
        destinationFile = layout.buildDirectory.file("jacoco/systemTest.exec").get().asFile
    }
    doFirst {
        systemProperty("systemTest.jacocoAgentJar", jacocoRuntimeAgent.singleFile.absolutePath)
        delete(layout.buildDirectory.file("jacoco/systemTest.exec"), layout.buildDirectory.file("jacoco/systemTest-app.exec"))
    }
    finalizedBy("jacocoSystemTestReport")
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    executionData(layout.buildDirectory.file("jacoco/test.exec"))
    sourceDirectories.setFrom(sourceSets.main.get().allSource.srcDirs)
    classDirectories.setFrom(sourceSets.main.get().output)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoSystemTestReport") {
    dependsOn(tasks.named("systemTest"))
    executionData(layout.buildDirectory.file("jacoco/systemTest-app.exec"))
    sourceDirectories.setFrom(sourceSets.main.get().allSource.srcDirs)
    classDirectories.setFrom(sourceSets.main.get().output)
    reports {
        xml.required = true
        html.required = true
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/systemTest"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/systemTest/jacocoSystemTestReport.xml"))
    }
}

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoAllTestReport") {
    dependsOn(tasks.test, tasks.named("systemTest"))
    executionData(
        layout.buildDirectory.file("jacoco/test.exec"),
        layout.buildDirectory.file("jacoco/systemTest-app.exec")
    )
    sourceDirectories.setFrom(sourceSets.main.get().allSource.srcDirs)
    classDirectories.setFrom(sourceSets.main.get().output)
    reports {
        xml.required = true
        html.required = true
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/allTests"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/allTests/jacocoAllTestReport.xml"))
    }
}

val dbHost = project.findProperty("db.host") as? String ?: "localhost"
val dbPort = project.findProperty("db.port") as? String ?: "5432"
val dbName = project.findProperty("db.name") as? String ?: "web"
val dbUser = project.findProperty("db.user") as? String ?: "postgres"
val dbPassword = project.findProperty("db.password") as? String ?: "postgres"
val dbUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"

flyway {
    url = dbUrl
    user = dbUser
    password = dbPassword
    locations = arrayOf("filesystem:db_scripts")
}

tasks.register("initDb") {
    group = "Database"
    description = "Инициализация схемы базы данных"
    
    doLast {
        exec {
            executable = "psql"
            args = listOf(
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-f", "db_scripts/db_init.sql"
            )
            environment("PGPASSWORD" to dbPassword)
        }
        println("БД инициализирована")
    }
}

tasks.register("fillDb") {
    group = "Database"
    description = "Заполнение БД тестовыми данными"
    dependsOn("initDb")
    
    doLast {
        exec {
            executable = "psql"
            args = listOf(
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-f", "db_scripts/db_fill.sql"
            )
            environment("PGPASSWORD" to dbPassword)
        }
        println("БД заполнена тестовыми данными")
    }
}

tasks.register("cleanDb") {
    group = "Database"
    description = "Очистка всех таблиц в базе данных"
    
    doLast {
        val dropTablesScript = """
            DO $$ DECLARE
                r RECORD;
            BEGIN
                FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                    EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
                END LOOP;
            END $$;
        """.trimIndent()
        
        exec {
            executable = "psql"
            args = listOf(
                "-h", dbHost,
                "-p", dbPort,
                "-U", dbUser,
                "-d", dbName,
                "-c", dropTablesScript
            )
            environment("PGPASSWORD" to dbPassword)
        }
        println("БД очищена")
    }
}

tasks.register("resetDb") {
    group = "Database"
    description = "Полная переинициализация БД (очистка и инициализация)"
    dependsOn("cleanDb", "initDb")
    
    doLast {
        println("БД успешно переинициализирована")
    }
}

tasks.register("setupDb") {
    group = "Database"
    description = "Инициализация и заполнение БД (полная подготовка)"
    dependsOn("fillDb")
    
    doLast {
        println("Окружение готово к работе")
    }
}

tasks.register("deploy") {
    group = "Application"
    description = "Сборка и запуск приложения в фоне"
    dependsOn(tasks.named("bootWar"))

    doLast {
        val pgReady = ProcessBuilder("pg_isready", "-h", dbHost, "-p", dbPort)
            .start().also { it.waitFor() }.exitValue() == 0

        if (!pgReady) {
            println("PostgreSQL недоступен, запускаю контейнер Docker...")
            val running = ProcessBuilder("docker", "ps", "-q", "--filter", "name=web-postgres")
                .start().also { it.waitFor() }.inputStream.bufferedReader().readText().trim()
            if (running.isEmpty()) {
                ProcessBuilder(
                    "docker", "run", "-d", "--rm", "--name", "web-postgres",
                    "-e", "POSTGRES_DB=$dbName", "-e", "POSTGRES_USER=$dbUser",
                    "-e", "POSTGRES_PASSWORD=$dbPassword", "-p", "$dbPort:5432",
                    "postgres:15-alpine"
                ).inheritIO().start().waitFor()
            }
            print("Ожидание готовности PostgreSQL")
            for (i in 1..30) {
                Thread.sleep(1000)
                print(".")
                val r = ProcessBuilder("pg_isready", "-h", dbHost, "-p", dbPort).start()
                r.waitFor()
                if (r.exitValue() == 0) break
            }
            println()
            val initPb = ProcessBuilder("psql", "-h", dbHost, "-p", dbPort, "-U", dbUser, "-d", dbName, "-f", "db_scripts/db_init.sql")
            initPb.environment()["PGPASSWORD"] = dbPassword
            initPb.inheritIO().start().waitFor()
            val fillPb = ProcessBuilder("psql", "-h", dbHost, "-p", dbPort, "-U", dbUser, "-d", dbName, "-f", "db_scripts/db_fill.sql")
            fillPb.environment()["PGPASSWORD"] = dbPassword
            fillPb.inheritIO().start().waitFor()
            println("БД инициализирована")
        }

        val warFile = tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar").get().archiveFile.get().asFile
        val process = ProcessBuilder("java", "-jar", warFile.absolutePath)
            .inheritIO()
            .start()
        println("Приложение запущено, PID: ${process.pid()}")
        println("Остановить: kill ${process.pid()}")
    }
}
