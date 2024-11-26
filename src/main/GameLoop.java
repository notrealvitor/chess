package main;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import exceptions.ChessException;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class GameLoop {
    private final ChessMatch chessMatch;
    private final List<ChessPiece> capturedPieces;
    private final Scanner scanner;

    private volatile String input = null; // Shared variable for input



    public GameLoop() {
        this.chessMatch = new ChessMatch();
        this.capturedPieces = new ArrayList<>();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (!chessMatch.isCheckMate()) {
            try {
                displayGameState();
                ChessPosition source = getSourcePosition();
                displayPossibleMoves(source);

                ChessPosition target = getTargetPosition();
                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

                updateCapturedPieces(capturedPiece);
                handlePromotion();

                updateJavaFXBoard();
            } catch (ChessException | InputMismatchException e) {
                System.out.println(e.getMessage());
                scanner.nextLine(); // Clear invalid input
            }
        }
        endGame();
    }

    private void displayGameState() {
        UI.clearScreen();
        UI.printMatch(chessMatch, capturedPieces);
    }

    private ChessPosition getSourcePosition() {
        System.out.print("Select a piece (A1 to H8): ");

        synchronized (this) {
            while (input == null) {
                try {
                    // Check for console input
                    if (scanner.hasNextLine()) {
                        input = scanner.nextLine(); // Read console input
                    } else {
                        this.wait(); // Wait for injected input
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        ChessPosition position = UI.readChessPosition(new Scanner(input));
        input = null; // Reset for next input
        return position;
    }

    private ChessPosition getTargetPosition() {
        System.out.print("Select a destination (A1 to H8): ");

        synchronized (this) { // Synchronize on the current instance
            while (input == null) {
                try {
                    // Check for console input
                    if (scanner.hasNextLine()) {
                        input = scanner.nextLine(); // Read console input
                    } else {
                        this.wait(); // Wait for injected input
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Handle interrupt
                }
            }
        }

        ChessPosition position = UI.readChessPosition(new Scanner(input));
        input = null; // Reset for next input
        return position;
    }

    public void injectPositionInput(String value) {
        synchronized (this) { // Synchronize on the same instance as getSourcePosition
            if (input == null) {
                input = value.toLowerCase(); // Inject the provided input
                System.out.println("\nInjected input: " + value.toUpperCase());
                this.notify(); // Notify the waiting thread
            }
        }
    }

    private void displayPossibleMoves(ChessPosition source) {
        boolean[][] possibleMoves = chessMatch.possibleMoves(source);
        UI.clearScreen();
        UI.printBoard(chessMatch.getPieces(), possibleMoves);
    }

    private void updateCapturedPieces(ChessPiece capturedPiece) {
        if (capturedPiece != null) {
            capturedPieces.add(capturedPiece);
        }
    }

    private void handlePromotion() {
        if (chessMatch.getPromoted() != null) {
            System.out.print("Enter piece for promotion (B/N/R/Q): ");
            String type = scanner.nextLine().toUpperCase();

            while (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
                System.out.print("Invalid value! Enter piece for promotion (B/N/R/Q): ");
                type = scanner.nextLine().toUpperCase();
            }

            chessMatch.replacePromotedPiece(type);
        }
    }

    private void updateJavaFXBoard() {
        javafx.application.Platform.runLater(() -> {
            JavaFX_UI.getInstance().updateChessBoard(chessMatch.getPieces());
        });
    }

    private void endGame() {
        UI.clearScreen();
        UI.printMatch(chessMatch, capturedPieces);
        System.out.println("Checkmate! Winner: " + chessMatch.getCurrentPlayer().getDescription().toUpperCase());
    }

    public ChessMatch getChessMatch() {
        return chessMatch; // Allow access to the current chess match
    }

    public void processNextTurn() {
        try {
            // Continue reading from System.in after simulated input
            ChessPosition source = getSourcePosition();
            ChessPosition target = getTargetPosition();

            // Process the move as usual
            ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

            // Additional game logic here...
        } catch (Exception e) {
            System.out.println("Error processing next turn: " + e.getMessage());
        }
    }

}