package eu.telecomnancy.application;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeNode {
    private String label;
    private ArrayList<TreeNode> sons;
    private static int currentId = 0;
    private int id;

    public TreeNode(String label, TreeNode... sons) {
        this.label = label;
        this.id = currentId;
        this.sons = new ArrayList<TreeNode>();
        this.addAllSons(sons);
        currentId++;
    }

    public boolean isLeaf() {
        return this.sons.isEmpty();
    }

    public void addSon(TreeNode son) {
        this.sons.add(son);
    }

    public void addAllSons(TreeNode... sons) {
        for (TreeNode son : sons) {
            this.addSon(son);
        }
    }

    public void addAllSons(ArrayList<TreeNode> sons) {
        for (TreeNode son : sons) {
            this.addSon(son);
        }
    }

    public void addFirstSon(TreeNode son){
        ArrayList<TreeNode> oldSons = this.getSons();
        ArrayList<TreeNode> newSons = new ArrayList<>();
        newSons.add(son);
        newSons.addAll(oldSons);

        this.setSons(newSons);
    }

    public ArrayList<TreeNode> getSons() {
        return this.sons;
    }

    public TreeNode getLastSon() {
        if (this.sons.isEmpty()) {
            return null;
        }
        return this.sons.get(this.sons.size() - 1);
    }

    public void setSons(ArrayList<TreeNode> sons) {
        this.sons = sons;
    }

    public String getLabel() {
        return this.label;
    }

    public int getId() {
        return this.id;
    }

    public static void newTree() {
        currentId = 0;
    }

    public int nbNodes() {
        int sum = 0;
        for (TreeNode son : this.getSons()) {
            sum += son.nbNodes();
        }
        return 1 + sum;
    }

    public String parcoursProfondeur() {

        // passed.get(node.getId()) = true si et seulement si node n'a pas été parcouru
        ArrayList<Boolean> free = new ArrayList<>(this.nbNodes());

        for (int i = 0; i < this.nbNodes(); i++) {
            free.add(true);
        }

        return parcoursProfondeur(this, free);
    }

    private String parcoursProfondeur(TreeNode node, ArrayList<Boolean> free) {

        StringBuilder stringBuilder = new StringBuilder();

        // On regarde tous les fils du noeud courant
        for (TreeNode son : node.getSons()) {

            // Si le noeud n'a pas était parcouru, on le parcourt.
            if (free.get(node.getId())) {
                free.set(node.getId(), false);
                stringBuilder.append(parcoursProfondeur(son, free));
            }
        }

        return stringBuilder.toString();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void firstClean() {
        TreeNode newTree = new TreeNode("root");
        List<TreeNode> currentSons = new ArrayList<>(getSons());
        List<String> toRemove = Arrays.asList(";", "LIB", "use", "is", "begin", "(", ")", "fich3", ",", ":", "then");
    
        Map<String, String> labelMap = createLabelMap();
    
        boolean isProcedure = false;
        boolean hasRemovedID = false;
    
        for (TreeNode child : currentSons) {
            String label = child.getLabel();
            if (this.label.equals("PROCEDURE") && !hasRemovedID && label.equals("VARIABLE")) {
                hasRemovedID = true;
                continue;
            }
            if ("procedure".equals(label)) {
                isProcedure = true;
            }
            if ("end".equals(label) && isProcedure) {
                isProcedure = false;
                continue;
            }
            if (!toRemove.contains(label) && !(isProcedure && "VARIABLE".equals(label))) {
                processTreeNode(newTree, child, labelMap);
            }
        }
    
        this.sons = newTree.getSons();
        this.sons.forEach(TreeNode::firstClean);

        this.verticalClean(false);
        this.verticalClean(false);
        this.verticalClean(true);
    }

    public void cleanTree(){
        this.firstClean();
        this.afterClean();
    }


    private void processTreeNode(TreeNode newTree, TreeNode child, Map<String, String> labelMap) {
        String label = child.getLabel();
        if (labelMap.containsKey(label)) {
            child.setLabel(labelMap.get(label));
        }
        newTree.addSon(child);
    }


    private Map<String, String> createLabelMap() {
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put("with", "DECL_LIB");
        labelMap.put("fich2", "PROCEDURE");
        return labelMap;
    }

    private void verticalClean(boolean vexpr) {
        List<String> toFactor = Arrays.asList("instr+1", "instr+", "expr1", "P1", "F", "I", "T1", "Pr", "expr1",
                "I1", "P", "expr+,", "expr+,1", "decl21", "decl22", "decl23", "decl31", "decl13", "decl+1", "decl+", "procedure",
                "param;+", "param;+1", "param2", "params", "id,+", "id,+1", "return", "end", "instr11", "instr1");
    
        TreeNode newTree = new TreeNode("root");
        boolean isFunction = false;
        String exprName = null;
        boolean isEqual = false;



        for (TreeNode child : this.sons) {
            String label = child.getLabel();
            if ("instr".equals(this.label) && "put".equals(label)) {
                this.setLabel("PUT");
            } else if("instr".equals(this.label) && "if".equals(label)){
                this.setLabel("IF");
            }else if("instr3".equals(this.label) && "else".equals(label)){
                this.setLabel("ELSE");
            }else if ("decl".equals(this.label) && "procedure".equals(label)) {
                this.setLabel("PROCEDURE");
            } else if ("decl".equals(this.label) && "function".equals(label)) {
                this.setLabel("FUNCTION");
                isFunction = true;
            } else if (isFunction) {
                isFunction = false;
            } else if ("expr".equals(this.label) && "+".equals(label)) {
                this.setLabel("+");
                isEqual = true;
            } else if ("expr".equals(this.label) && "-".equals(label)) {
                this.setLabel("-");
                isEqual = true;
            } else if ("expr".equals(this.label) && "*".equals(label)) {
                this.setLabel("*");
                isEqual = true;
            } else if ("expr".equals(this.label) && "/".equals(label)) {
                this.setLabel("/");
                isEqual = true;
            } else if ("expr".equals(this.label) && "rem".equals(label)) {
                this.setLabel("rem");
                isEqual = true;
            } else if (("instr1".equals(this.label)) && "=".equals(label)) {
                this.setLabel("=");
            }  else if (!vexpr && ("instr".equals(this.label)) && "=".equals(label)) {
                this.setLabel("=");
                newTree.addAllSons(child.getSons().toArray(new TreeNode[0]));
            } else if(vexpr && "expr".equals(this.label) && exprName == null && !isEqual){
                exprName = label;
            }else if ("expr".equals(this.label) && "=".equals(label)) {
                if(vexpr){
                    this.setLabel("=");
                    newTree.addAllSons(child.getSons().toArray(new TreeNode[0]));
                    isEqual = true;
                }
            }else if (toFactor.contains(label)) {
                newTree.addAllSons(child.getSons().toArray(new TreeNode[0]));
            } else {
                newTree.addSon(child);
            }
        }
        if(isEqual && exprName != null){
            TreeNode expr = new TreeNode(exprName);
            ArrayList<TreeNode> sons = newTree.getSons();
            sons.add(0, expr);
            newTree.setSons(sons);
        }
        if(exprName != null && !isEqual){
            this.setLabel(exprName);
        }
        removeEndVariable(newTree);
        this.sons = newTree.getSons();
    }

    
    
    

    private void removeEndVariable(TreeNode newTree) {
        TreeNode cleanedTree = new TreeNode("root");
        boolean isVariable = false;

        for (TreeNode child : newTree.getSons()) {
            String label = child.getLabel();
            if ("VARIABLE".equals(label)) {
                isVariable = true;
                continue;
            } else if (isVariable && "end".equals(label)) {
                isVariable = false;
                continue;
            } else {
                cleanedTree.addSon(child);
            }
        }

        if (isVariable) {
            cleanedTree.addSon(new TreeNode("VARIABLE"));
        }

        newTree.setSons(cleanedTree.getSons());
    }

    public void afterClean(){
        if ("T".equals(this.label)){
            if (this.getSons().size() == 1){
                this.setLabel(this.getSons().get(0).getLabel());
                this.getSons().remove(0);
            }
            else if (this.getSons().size() > 1) {
                for (TreeNode child : this.sons){
                    if (child.getLabel().equals("*")){
                        this.setLabel("*");
                        this.getSons().remove(child);
                        break;
                    }
                    if (child.getLabel().equals("/")){
                        this.setLabel("/");
                        this.getSons().remove(child);
                        break;
                    }
                }
            }
        }
        this.sons.forEach(TreeNode::afterClean);
    }

}
