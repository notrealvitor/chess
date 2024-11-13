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

import java.util.concurrent.CountDownLatch;


public class JavaFX_UI extends Application {

    private GridPane gridPane; // GridPane for the chessboard
    private ChessMatch chessMatch; // The game logic object
    private static JavaFX_UI instance; // Singleton instance
    private static CountDownLatch initLatch;

    @Override
    public void start(Stage primaryStage) {
        instance = this; // Set the singleton instance
        if(initLatch!=null){  initLatch.countDown();    }

        chessMatch = new ChessMatch(); // Initialize game logic

        BorderPane root = new BorderPane();

        // Add a MenuBar to the top
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        // Create and configure the chessboard
        gridPane = new GridPane();
        updateChessBoard(chessMatch.getPieces());
        root.setCenter(gridPane);
        gridPane.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 600, 650);
        primaryStage.setTitle("Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Update the chessboard dynamically
    public void updateChessBoard(ChessPiece[][] pieces) {
        gridPane.getChildren().clear();

        for (int row = 0; row < pieces.length; row++) {
            for (int col = 0; col < pieces[row].length; col++) {
                Button square = new Button();
                square.setPrefSize(60, 60);

                ChessPiece piece = pieces[row][col];
                square.setText(piece != null ? piece.toString() : "");

                if ((row + col) % 2 == 0) {
                    square.setStyle("-fx-background-color: white;");
                } else {
                    square.setStyle("-fx-background-color: gray;");
                }

                gridPane.add(square, col, row);

                // Add click handling for future logic
                int currentRow = row;
                int currentCol = col;
                square.setOnAction(e -> handleSquareClick(currentRow, currentCol));
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

    // Placeholder for handling square clicks
    private void handleSquareClick(int row, int col) {
        System.out.println("Square clicked: " + row + ", " + col);
        // Placeholder: Add game move logic
    }

    public static void setInitLatch(CountDownLatch latch) {
        initLatch = latch;}

//in start()


}



