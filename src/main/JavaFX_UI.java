package main;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import java.util.InputMismatchException;
import javafx.stage.Stage;
import exceptions.ChessException;
import java.util.ArrayList;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;

import java.util.concurrent.CountDownLatch;


public class JavaFX_UI extends Application {

    private GridPane gridPane; // GridPane for the chessboard
    private ChessMatch chessMatch; // The game logic object
    private static JavaFX_UI instance; // Singleton instance
    private static CountDownLatch initLatch;

    private GameState currentState = GameState.SELECT_PIECE;
    private ChessPosition selectedSource = null;

    private HashMap<String, Label> footerLabels = new HashMap<>();

    private enum GameState {
        SELECT_PIECE,
        SELECT_DESTINATION
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;

        chessMatch = new ChessMatch();
        BorderPane root = new BorderPane();

        // Add a MenuBar to the top
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Create and configure the chessboard
        gridPane = new GridPane();
        updateChessBoard(chessMatch.getPieces());
        root.setCenter(gridPane);
        gridPane.setAlignment(Pos.TOP_CENTER);

        // Create and add the footer
        VBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 600, 700);  // Adjust height to fit footer
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createFooter() {
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER);
        footer.setSpacing(10);

        Label currentPlayerLabel = new Label("Current Player: " + chessMatch.getCurrentPlayer().getDescription().toUpperCase());
        Label capturedPiecesLabel = new Label("Captured Pieces: None");

        footer.getChildren().addAll(currentPlayerLabel, capturedPiecesLabel);

        // Store labels for dynamic updates
        footerLabels.put("currentPlayer", currentPlayerLabel);
        footerLabels.put("capturedPieces", capturedPiecesLabel);

        return footer;
    }

    // chessboard without possible moves
    public void updateChessBoard(ChessPiece[][] pieces) {
        updateChessBoard(pieces, null); // Call the method with `null` for possible moves
    }
    // Update the chessboard dynamically
    public void updateChessBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        gridPane.getChildren().clear();  // Clear the grid before updating

        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                Button square = new Button();
                square.setPrefSize(60, 60);  // Set button size

                ChessPiece piece = pieces[row][col];
                if (piece != null) {
                    String pieceName = piece.getColor().toString().toLowerCase() + "-" + piece.toString().toLowerCase();
                    String imagePath = "/Textures/" + pieceName + ".png";

// Debugging: Get the absolute path of the resource
                    URL resourceUrl = getClass().getResource(imagePath);
                    if (resourceUrl != null) {
                        System.out.println("Resolved system path: " + resourceUrl.getPath());
                    } else {
                        System.err.println("Failed to resolve system path for: " + imagePath);
                    }

// Load the image
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(50);  // Adjust image size to fit the button
                    imageView.setFitWidth(50);


                    square.setGraphic(imageView);  // Set image as button content
                } else {
                    square.setGraphic(null);  // No piece, clear the button graphic
                }

                // Highlight possible moves if provided
                if (possibleMoves != null && possibleMoves[row][col]) {
                    square.setStyle("-fx-background-color: lightblue;");
                } else if ((row + col) % 2 == 0) {
                    square.setStyle("-fx-background-color: white;");
                } else {
                    square.setStyle("-fx-background-color: gray;");
                }

                // Set the square's ID and add it to the grid
                String squareID = (char) ('a' + col) + String.valueOf(8 - row);
                square.setId(squareID);

                gridPane.add(square, col, row);

                // Set button action
                square.setOnAction(e -> handleSquareClick(squareID));
            }
        }
    }

    public ChessMatch getChessMatch() {
        return chessMatch;
    }

    public static JavaFX_UI getInstance() {
        return instance; // Return the singleton instance
    }

    public static void main(String[] args) {
        launch(args); // Start JavaFX application
    }

    // Create a MenuBar with basic options
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Create "Game" menu
        Menu gameMenu = new Menu("Game");
        MenuItem newGame = new MenuItem("New Game");
        MenuItem exitGame = new MenuItem("Exit");

        // Add actions for menu items
        newGame.setOnAction(e -> {
            chessMatch = new ChessMatch(); // Reset game logic
            updateChessBoard(chessMatch.getPieces()); // Refresh chessboard
            System.out.println("New Game Started!");
        });

        exitGame.setOnAction(e -> System.exit(0)); // Exits the application

        gameMenu.getItems().addAll(newGame, exitGame);

        // Add menus to the menu bar
        menuBar.getMenus().addAll(gameMenu);

        return menuBar;
    }

    private void handleSquareClick(String squareID) {
        try {
            ChessPosition clickedPosition = UI.clickChessPosition(squareID);

            if (currentState == GameState.SELECT_PIECE) {
                if (chessMatch.getPiece(clickedPosition) == null ||
                        chessMatch.getPiece(clickedPosition).getColor() != chessMatch.getCurrentPlayer()) {
                    System.out.println("Invalid piece selection. Please select a valid piece.");
                    return;
                }

                selectedSource = clickedPosition;
                currentState = GameState.SELECT_DESTINATION;
                updateChessBoard(chessMatch.getPieces(), chessMatch.possibleMoves(selectedSource));

            } else if (currentState == GameState.SELECT_DESTINATION) {
                ChessPiece capturedPiece = chessMatch.performChessMove(selectedSource, clickedPosition);
                currentState = GameState.SELECT_PIECE;

                updateChessBoard(chessMatch.getPieces());

                // Update footer
                updateFooter(capturedPiece);

                // Sync Console Board
                UI.clearScreen();
                UI.printMatch(chessMatch, new ArrayList<>());
            }
        } catch (ChessException | InputMismatchException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateFooter(ChessPiece capturedPiece) {
        Label currentPlayerLabel = footerLabels.get("currentPlayer");
        Label capturedPiecesLabel = footerLabels.get("capturedPieces");

        // Update current player turn
        currentPlayerLabel.setText("Current Player: " + chessMatch.getCurrentPlayer().getDescription().toUpperCase());

        // Update captured pieces
        if (capturedPiece != null) {
            String currentCaptured = capturedPiecesLabel.getText();
            currentCaptured += " " + capturedPiece;
            capturedPiecesLabel.setText("Captured Pieces: " + currentCaptured);
        }
    }

    private void handlePromotion() {
        TextInputDialog dialog = new TextInputDialog("Q");
        dialog.setTitle("Promotion");
        dialog.setHeaderText("Choose piece for promotion (B/N/R/Q):");
        dialog.setContentText("Enter B, N, R, or Q:");

        String type = dialog.showAndWait().orElse("Q").toUpperCase();
        while (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            type = dialog.showAndWait().orElse("Q").toUpperCase();
        }

        chessMatch.replacePromotedPiece(type);
    }

    public static void setInitLatch(CountDownLatch latch) {
        initLatch = latch;}

//in start()


}



