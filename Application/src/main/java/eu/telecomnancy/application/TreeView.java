package eu.telecomnancy.application;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class TreeView extends Pane {
    private TreeNode tree;
    private double width;
    private double height;
    private double hGap;
    private double fontSize;

    TreeView(TreeNode tree, double width, double height) {
        super();
        this.tree = tree;
        this.hGap = 50;
        this.width = width;
        this.height = height;
        this.fontSize = 20;
        this.setMinSize(width, height);
        this.displayTree();
    }

    TreeView(TreeNode tree, double width, double height, double fontSize) {
        super();
        this.tree = tree;
        this.hGap = 50;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
        this.setMinSize(width, height);
        this.displayTree();
    }



    TreeView(TreeNode tree, double width, double height, double fontSize, double hGap) {
        super();
        this.tree = tree;
        this.hGap = hGap;
        this.width = width;
        this.height = height;
        this.fontSize = fontSize;
        this.setMinSize(width, height);
        this.displayTree();
    }

    public void displayTree() {
        this.getChildren().clear(); // Clear the pane
        if (this.tree != null) {
            // Display tree recursively
            this.displayTree(this.tree, this.width/2, this.height/10, 0, this.width);
        }
    }

    /**
     * Display a subtree rooted at position (x, y)
     */
    private void displayTree(TreeNode root, double x, double y, double x0, double nodeWidth) {

        int nbSons = root.getSons().size();
        double vGap;
        if (nbSons != 0)
            vGap = nodeWidth/nbSons;
        else
            vGap = -1;
        double newX = x0 + vGap/2;


        for (TreeNode node : root.getSons()){
            Line line = new Line(newX, y + hGap, x, y);
            this.getChildren().add(line);
            displayTree(node, newX, y + hGap, newX - vGap/2, vGap);
            newX += vGap;
        }

        // Display a node
        
        Text text = new Text(root.getLabel());
        text.setFont(new Font(this.fontSize));
        int nbLetters = root.getLabel().length();
        
        double rectWidth = this.fontSize*nbLetters/1.5;
        Rectangle rectangle = new Rectangle(rectWidth, this.fontSize);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(rectangle, text);
        stack.setLayoutX(x - rectWidth/2);
        stack.setLayoutY(y - this.fontSize / 2);

        this.getChildren().add(stack);
    }
}

