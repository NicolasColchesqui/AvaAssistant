package slime.AvaAssistant.desktop;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import slime.AvaAssistant.AvaAssistantApplication;
import slime.AvaAssistant.service.OllamaRuntimeManager;

/** Single IntelliJ entry point: starts Ollama (if needed), Spring Boot and JavaFX. */
public class AvaAssistantDesktopLauncher extends Application {

    private ConfigurableApplicationContext springContext;
    private final OllamaRuntimeManager ollamaManager = new OllamaRuntimeManager();

    @Override
    public void init() {
        ollamaManager.startAndValidate();
        springContext = new SpringApplicationBuilder(AvaAssistantApplication.class)
                .web(WebApplicationType.SERVLET)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        AvaAssistantApp desktopApp = new AvaAssistantApp();
        desktopApp.start(primaryStage);
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        ollamaManager.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
