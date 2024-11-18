package main;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author joana
 */

public class Main {
    public static void main(String[] args) {
        // Latch to wait until JavaFX UI is ready
        CountDownLatch latch = new CountDownLatch(1);


        new Thread(() -> {
            JavaFX_UI.setInitLatch(latch);

            JavaFX_UI.main(args); // Launch JavaFX UI
        }).start();

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX UI initialization timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Retrieve JavaFX_UI instance and set GameLoop
        JavaFX_UI uiInstance = JavaFX_UI.getInstance();
        if (uiInstance == null) {
            throw new IllegalStateException("JavaFX_UI instance is not initialized.");
        }

        GameLoop gameLoop = new GameLoop();
        uiInstance.setGameLoop(gameLoop); // Pass GameLoop instance to JavaFX_UI
        gameLoop.start();
    }
}