package exceptions;

import main.JavaFX_UI;
import main.Main;

import java.io.PrintWriter;

/**
 *
 * @author joana
 */
public class ChessException extends BoardException {
    public ChessException(String msg) {
        super(msg);
        autoHandleException(msg);
    }

    private void autoHandleException(String msg) {
        System.out.println(msg); // Print the exception message
        System.out.println("Retrying automatically...");

        Main.AllowInjectedInput(true);

        PrintWriter injector = JavaFX_UI.getInputInjector(); // Get the injector from JavaFX_UI

        if (injector != null) {
            String fallbackInput = "a2"; // Example fallback input (adjust dynamically as needed)
            injector.println(fallbackInput);  // Inject fallback input
            injector.flush();
        } else {
            System.err.println("Input injector is not initialized!");
        }
    }
}