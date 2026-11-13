package slime.AvaAssistant.service;

import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves bundled tools from the installed app directory, with development fallbacks. */
public final class AppRuntimePaths {

    private AppRuntimePaths() { }

    public static Path appDirectory() {
        String packagedPath = System.getProperty("jpackage.app-path");
        if (packagedPath != null && !packagedPath.isBlank()) {
            Path launcher = Path.of(packagedPath).toAbsolutePath();
            if (launcher.getParent() != null) return launcher.getParent();
        }

        try {
            Path location = Path.of(AppRuntimePaths.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).toAbsolutePath();
            return Files.isDirectory(location) ? location : location.getParent();
        } catch (Exception ignored) {
            return Path.of(System.getProperty("user.dir")).toAbsolutePath();
        }
    }

    public static Path bundled(String relativePath) {
        return appDirectory().resolve(relativePath).normalize();
    }
}
