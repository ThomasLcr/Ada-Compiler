package eu.telecomnancy.application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JFX_Demo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFx Tree Test");

        double treeWidth = 800;
        double treeHeight = 400;

        double sceneWidth = 1520;
        double sceneHeight = 900;

        TreeView treeView = getTreeView(treeWidth, treeHeight);
        treeView.setLayoutX((sceneWidth - treeWidth)/2);
        treeView.setLayoutY((sceneHeight - treeHeight)/2);

        Scene scene = new Scene(treeView, sceneWidth, sceneHeight);
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static TreeView getTreeView(double treeWidth, double treeHeight) {
        TreeNode leaf1 = new TreeNode("1");
        TreeNode leaf2 = new TreeNode("2");
        TreeNode leaf3 = new TreeNode("3");
        TreeNode leaf4 = new TreeNode("4");
        TreeNode leaf5 = new TreeNode("5");
        TreeNode leaf6 = new TreeNode("6");

        TreeNode node1 = new TreeNode("7");
        TreeNode node2 = new TreeNode("8");

        TreeNode root = new TreeNode("root");

        node1.addAllSons(leaf1, leaf2, leaf3);

        node2.addAllSons(leaf4, leaf5);

        root.addAllSons(node1, leaf6, node2);

        TreeView treeView = new TreeView(root, treeWidth, treeHeight);
        return treeView;
    }
}
