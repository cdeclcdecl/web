plugins {
    id("org.flywaydb.flyway") version "9.22.3"
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
