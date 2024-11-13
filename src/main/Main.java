package main;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import exceptions.ChessException;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Latch to wait until JavaFX UI is ready
        CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            JavaFX_UI.setInitLatch(latch);
            JavaFX_UI.main(args); // Launch JavaFX UI
        }).start();

        try {
            // Wait for JavaFX to finish initializing
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX UI initialization timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } // Launch JavaFX in a separate thread so the console do not freeze


        Scanner sc = new Scanner(System.in);
        ChessMatch chessMatch = new ChessMatch();
        List<ChessPiece> captured = new ArrayList<>();

        while(!chessMatch.isCheckMate()) {
            try {
                UI.clearScreen();
                UI.printMatch(chessMatch, captured);

                //System.out.println();
                System.out.print("Waiting valid " + chessMatch.getCurrentPlayer().getDescription().toUpperCase() + " piece location(A1 to H8): \n ");
                //System.out.print("Source: ");
                System.out.flush(); // testing a bug fix for typing not appearing in cmd
                ChessPosition source = UI.readChessPosition(sc);

                // Update the JavaFX
                javafx.application.Platform.runLater(() -> {
                    JavaFX_UI.getInstance().updateChessBoard(chessMatch.getPieces());
                });

                boolean[][] possibleMoves = chessMatch.possibleMoves(source);
                UI.clearScreen();
                UI.printBoard(chessMatch.getPieces(), possibleMoves);
                
                //System.out.println();
                System.out.print("Waiting valid destination for your piece(A1 to H8): \n ");
                //System.out.print("Target: ");
                ChessPosition target = UI.readChessPosition(sc);
                System.out.println();

                ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

                // Update the JavaFX Board
                javafx.application.Platform.runLater(() -> {
                    JavaFX_UI.getInstance().updateChessBoard(chessMatch.getPieces());
                });

                if(capturedPiece != null) 
                    captured.add(capturedPiece);
                
                if(chessMatch.getPromoted() != null){
                    System.out.print("Enter piece for promotion (B/N/R/Q): ");
                    String type = sc.nextLine().toUpperCase();
                    
                    while(!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")){
                        System.out.print("Invalid value! Enter piece for promotion (B/N/R/Q): ");
                        type = sc.nextLine().toUpperCase();
                    }
                    
                    chessMatch.replacePromotedPiece(type);
                }
                
            } catch (ChessException | InputMismatchException e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            }
        }
        UI.clearScreen();
        UI.printMatch(chessMatch, captured);
    }
}