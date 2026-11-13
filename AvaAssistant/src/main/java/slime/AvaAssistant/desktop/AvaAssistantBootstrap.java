package slime.AvaAssistant.desktop;

import javafx.application.Application;

/**
 * Entry point for IntelliJ. This class deliberately does NOT extend
 * javafx.application.Application so the JVM does not try to launch JavaFX
 * before the JavaFX runtime modules are available on the classpath.
 */
public final class AvaAssistantBootstrap {

    private AvaAssistantBootstrap() {
    }

    public static void main(String[] args) {
        Application.launch(AvaAssistantDesktopLauncher.class, args);
    }
}
