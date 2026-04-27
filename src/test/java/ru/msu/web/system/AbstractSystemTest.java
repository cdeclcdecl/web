package ru.msu.web.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSystemTest {

    static final PostgreSQLContainer<?> POSTGRES;
    private static final Object APP_LOCK = new Object();
    private static Process appProcess;
    private static Path appLogFile;
    private static int appPort;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine")
                .withStartupTimeout(Duration.ofSeconds(180));
        POSTGRES.start();
        try (Connection conn = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db_init.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("db_fill.sql"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test database", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(AbstractSystemTest::stopApplication));
    }

    protected WebDriver driver;

    @BeforeEach
    void setUp() {
        ensureApplicationStarted();
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        java.io.File snapFirefox = new java.io.File("/snap/firefox/current/usr/lib/firefox/firefox");
        if (snapFirefox.exists()) {
            options.setBinary(snapFirefox.getAbsolutePath());
        }
        driver = new FirefoxDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String url(String path) {
        return "http://127.0.0.1:" + appPort + path;
    }

    private static void ensureApplicationStarted() {
        if (appProcess != null && appProcess.isAlive()) {
            return;
        }
        synchronized (APP_LOCK) {
            if (appProcess != null && appProcess.isAlive()) {
                return;
            }
            try {
                appPort = findFreePort();
                appLogFile = Path.of("build", "system-test-app.log");
                Files.createDirectories(appLogFile.getParent());
                Files.deleteIfExists(appLogFile);
                Files.createFile(appLogFile);
                ProcessBuilder processBuilder = new ProcessBuilder(
                        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                        "-jar",
                        findWarFile().toString(),
                        "--server.port=" + appPort
                );
                processBuilder.environment().put("DB_HOST", POSTGRES.getHost());
                processBuilder.environment().put("DB_PORT", String.valueOf(POSTGRES.getFirstMappedPort()));
                processBuilder.environment().put("DB_NAME", POSTGRES.getDatabaseName());
                processBuilder.environment().put("DB_USER", POSTGRES.getUsername());
                processBuilder.environment().put("DB_PASSWORD", POSTGRES.getPassword());
                processBuilder.redirectErrorStream(true);
                processBuilder.redirectOutput(appLogFile.toFile());
                appProcess = processBuilder.start();
                waitUntilReady();
            } catch (Exception e) {
                stopApplication();
                throw new RuntimeException("Failed to start test application", e);
            }
        }
    }

    private static Path findWarFile() throws IOException {
        try (var files = Files.list(Path.of("build", "libs"))) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".war"))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("WAR file not found in build/libs"));
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void waitUntilReady() throws Exception {
        Instant deadline = Instant.now().plusSeconds(90);
        while (Instant.now().isBefore(deadline)) {
            if (appProcess == null || !appProcess.isAlive()) {
                throw new IllegalStateException(readLogTail());
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("http://127.0.0.1:" + appPort + "/employees/new").openConnection();
                connection.setConnectTimeout(1000);
                connection.setReadTimeout(1000);
                connection.setInstanceFollowRedirects(false);
                if (connection.getResponseCode() == 200) {
                    return;
                }
            } catch (IOException ignored) {
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException(readLogTail());
    }

    private static String readLogTail() {
        if (appLogFile == null || !Files.exists(appLogFile)) {
            return "Application log is unavailable";
        }
        try {
            String content = Files.readString(appLogFile);
            int start = Math.max(0, content.length() - 4000);
            return content.substring(start);
        } catch (IOException e) {
            return "Failed to read application log";
        }
    }

    private static void stopApplication() {
        if (appProcess == null) {
            return;
        }
        appProcess.destroy();
        try {
            if (!appProcess.waitFor(5, TimeUnit.SECONDS)) {
                appProcess.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            appProcess.destroyForcibly();
        }
        appProcess = null;
    }
}
