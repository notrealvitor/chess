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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

import java.io.PrintWriter;
import java.net.URL; // Needed for resource loading

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;


public class JavaFX_UI extends Application {

    private GridPane gridPane; // GridPane for the chessboard
    private ChessMatch chessMatch; // The game logic object
    private static JavaFX_UI instance; // Singleton instance
    private static CountDownLatch initLatch;

    private HashMap<String, Label> footerLabels = new HashMap<>();
    private HashMap<String, Image> pieceImages;
    private static PrintWriter inputInjector;

    @Override
    public void start(Stage primaryStage) {
        try {
            instance = this;
            chessMatch = new ChessMatch();

            preloadPieceImages(); // avoid the textures being loaded everytime

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
        }finally
        {
            if (initLatch != null) {
                initLatch.countDown(); // Signal readiness
            }
        }

    }

    // Update the chessboard dynamically
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

    private void preloadPieceImages() {
        pieceImages = new HashMap<>();
        String[] colors = {"white", "black"};
        String[] pieces = {"k", "q", "r", "b", "n", "p"};

        for (String color : colors) {
            for (String piece : pieces) {
                String pieceName = color + "-" + piece;
                String imagePath = "/Textures/" + pieceName + ".png";

                // Check if the resource exists
                URL resourceUrl = getClass().getResource(imagePath);
                if (resourceUrl == null) {
                    System.err.println("Image not found: " + imagePath);
                } else {
                    //System.out.println("Loading image: " + resourceUrl.getPath());
                    Image image = new Image(getClass().getResourceAsStream(imagePath));
                    pieceImages.put(pieceName, image);
                }
            }
        }
    }

    // chessboard without possible moves
    public void updateChessBoard(ChessPiece[][] pieces) {
        updateChessBoard(pieces, null); // Call the method with `null` for possible moves
    }
    // Update the chessboard dynamically
    public void updateChessBoard(ChessPiece[][] pieces, boolean[][] possibleMoves) {
        gridPane.getChildren().clear();

        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                Button square = new Button();
                square.setPrefSize(60, 60);

                ChessPiece piece = pieces[row][col];
                if (piece != null) {
                    String pieceName = piece.getColor().toString().toLowerCase() + "-" + piece.toString().toLowerCase();
                    Image image = pieceImages.get(pieceName);
                    if (image != null) {
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);
                        square.setGraphic(imageView);
                    }
                }

                // Highlight valid moves
                if (possibleMoves != null && possibleMoves[row][col]) {
                    square.setStyle("-fx-background-color: lightblue;");
                } else if ((row + col) % 2 == 0) {
                    square.setStyle("-fx-background-color: white;");
                } else {
                    square.setStyle("-fx-background-color: gray;");
                }

                String squareID = (char) ('a' + col) + String.valueOf(8 - row);
                square.setId(squareID);
                gridPane.add(square, col, row);

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
            Thread.sleep(100);
            Main.AllowInjectedInput(true);
            if (inputInjector != null) {
                inputInjector.println(squareID);  // Inject input
                inputInjector.flush();
                //System.out.println("Injected input clicked: " + squareID);
            }
        } catch (Exception e) {
            System.out.println("Error injecting input: " + e.getMessage());
        }
    }

    public static void setInitLatch(CountDownLatch latch) {
        initLatch = latch;}

    public static void setInputInjector(PrintWriter injector) {
        inputInjector = injector;
    }

    // New getter method
    public static PrintWriter getInputInjector() {
        return inputInjector;
    }
}



