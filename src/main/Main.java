package main;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import exceptions.ChessException;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author joana
 */
public class Main {
    private static PipedInputStream pipedInput;
    private static PipedOutputStream pipedOutput;
    private static Scanner injectedScanner;
    private static Scanner consoleScanner;
    private static boolean useInjectedInput = false; // Flag to switch input

    public static void main(String[] args) {
        try {
            pipedInput = new PipedInputStream();
            pipedOutput = new PipedOutputStream(pipedInput);
            injectedScanner = new Scanner(pipedInput);
            consoleScanner = new Scanner(System.in);
            PrintWriter inputInjector = new PrintWriter(pipedOutput, true);

            CountDownLatch latch = new CountDownLatch(1);
            new Thread(() -> {
                JavaFX_UI.setInitLatch(latch);
                JavaFX_UI.setInputInjector(inputInjector);
                JavaFX_UI.main(args);
            }).start();

            latch.await();  // Wait for JavaFX initialization

            ChessMatch chessMatch = new ChessMatch();
            List<ChessPiece> captured = new ArrayList<>();

            while (!chessMatch.isCheckMate()) {
                try {
                    UI.clearScreen();
                    UI.printMatch(chessMatch, captured);

                    // Piece selection
                    System.out.print("Waiting valid piece location (A1 to H8): ");
                    ChessPosition source = waitForValidInput();

                    boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                    UI.printBoard(chessMatch.getPieces(), possibleMoves);

                    // **Highlight valid moves on JavaFX board**
                    javafx.application.Platform.runLater(() -> JavaFX_UI.getInstance().updateChessBoard(chessMatch.getPieces(), possibleMoves));

                    // Target selection
                    System.out.print("Waiting valid destination (A1 to H8): ");
                    ChessPosition target = waitForValidInput();

                    ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
                    if (capturedPiece != null) captured.add(capturedPiece);

                    if (chessMatch.getPromoted() != null) {
                        System.out.print("Enter piece for promotion (B/N/R/Q): ");
                        String type = consoleScanner.nextLine().toUpperCase();
                        chessMatch.replacePromotedPiece(type);
                    }

                    // **Update JavaFX chessboard after the move**
                    javafx.application.Platform.runLater(() -> JavaFX_UI.getInstance().updateChessBoard(chessMatch.getPieces()));

                } catch (ChessException | InputMismatchException e) {
                    System.out.println(e.getMessage());
                    clearCurrentInput();  // Clear invalid input
                }
            }

            UI.printMatch(chessMatch, captured);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ChessPosition waitForValidInput() {
        ChessPosition position = null;
        while (position == null) {
            position = getDynamicInput();
            try {
                Thread.sleep(10);  // Prevent busy-waiting
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return position;
    }

    private static ChessPosition getDynamicInput() {
        try {
            if (useInjectedInput && injectedScanner.hasNextLine()) {
               // System.out.println("Reading injected input...");
                String injected = injectedScanner.nextLine();
                System.out.println("Injected input received: " + injected);
                AllowInjectedInput(false);
                return UI.readChessPosition(new Scanner(injected));
            } else if (!useInjectedInput && System.in.available() > 0) {
                return UI.readChessPosition(consoleScanner);
            }
            return null;  // No input available yet
        } catch (IOException e) {
            throw new RuntimeException("Error checking input availability", e);
        }
    }


    public static void AllowInjectedInput(boolean allowState) {
        useInjectedInput = allowState;
        //System.out.println("Injected input enabled: " + allowState);
    }



    private static void clearCurrentInput() {
        if (useInjectedInput) {
            injectedScanner.nextLine();  // Clear piped input
        } else {
            consoleScanner.nextLine();  // Clear console input
        }
    }
}