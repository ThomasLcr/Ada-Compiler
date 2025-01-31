package eu.telecomnancy.application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;



import eu.telecomnancy.application.tds.TDS;
import eu.telecomnancy.application.tds.TDSItemFctProc;
import eu.telecomnancy.application.tds.TDSItemVar;

public class ASMGenerator {
    private TDSItemFctProc tds;
    private TDS full_tds;
    private TDSItemFctProc backup_tds;
    private TreeNode ast;
    private String filename;
    private PrintWriter writer;

    // sections du fichier asm
    private ArrayList<String> dataSection = new ArrayList<>();
    private ArrayList<String> bssSection = new ArrayList<>();
    private ArrayList<String> textSection = new ArrayList<>();
    private ArrayList<String> startSection = new ArrayList<>();
    private ArrayList<String> functionsSection = new ArrayList<>();
    private ArrayList<String> macroSection = new ArrayList<>();
    private ArrayList<String> rodataSection = new ArrayList<>();


    private String functions = "";
    private int popCounter = 0;

    private String generateRandomString() {
        String randomString = "";
        for (int i = 0; i < 10; i++) {
            randomString += (char) (Math.random() * 26 + 97);
        }
        return randomString;
    }

    public ASMGenerator(TDSItemFctProc tds, TreeNode ast, String filename, TDS full_tds) {
        this.tds = tds;
        this.full_tds = full_tds;
        this.backup_tds = tds;
        this.ast = ast;
        this.filename = filename.split("\\.")[0];

        // création du fichier asm
        try {
            File dir = new File("bin/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File("bin/" + this.filename + ".asm");
            file.createNewFile();
            if (file.exists()) {
                PrintWriter writer = new PrintWriter(file);
                writer.write("");
                this.writer = writer;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        this.writer.close();
        // run generate_and_run_ASM.sh with the filename as argument
        ProcessBuilder pb = new ProcessBuilder("sh", "generate_and_run_asm.sh", this.filename);
        Process p;
        try {
            p = pb.start();
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

    }

    public void generate() {
        handleTreeNode(ast);
        recursiveGeneration(ast);
        startSection.add("    mov rax,60; appel système pour terminer");
        startSection.add("    xor rdi,rdi; code de retour 0");
        startSection.add("    syscall; interruption du noyau");
        macroSection.add("""
                %macro prologue 0
                        push    rbp
                        mov     rbp,rsp
                        push    rbx
                        push    r12
                        push    r13
                        push    r14
                        push    r15
                %endmacro
                %macro epilogue 0
                        pop     r15
                        pop     r14
                        pop     r13
                        pop     r12
                        pop     rbx
                        leave
                        ret
                %endmacro""");

        functionsSection.add("""

                print_string:
                    prologue
                    mov     rsi,rdi
                    mov     rdi,qword string_format
                    xor     rax,rax
                    call    printf
                    epilogue

                print_nl:
                    prologue
                    mov     edi, 0xA      ; Charge le code ASCII pour la nouvelle ligne dans rdi
                    call    putchar       ; Appelle putchar pour imprimer le caractère de nouvelle ligne
                    epilogue

                print_int:
                    prologue
                    ;integer arg is in rdi
                    mov     rsi, rdi
                    mov     rdi, dword int_format
                    xor     rax,rax
                    call    printf
                    epilogue

                read_int:
                    prologue
                    ;rdi is assumed to have the address of the int to be read in
                    mov     rsi, rdi
                    mov     rdi, dword int_format
                    xor     rax,rax
                    call    scanf
                    epilogue

                print_float:
                    prologue
                    ;float arg is in st0
                    mov     rsi, rsp
                    fstp    qword [rsp]
                    mov     rdi, qword float_format
                    xor     rax,rax
                    call    printf
                    epilogue

                        """);
        for (String line : macroSection) {
            writer.println(line);
        }
        for (String line : dataSection) {
            writer.println(line);
        }
        for (String line : rodataSection) {
            writer.println(line);
        }
        for (String line : bssSection) {
            writer.println(line);
        }
        for (String line : textSection) {
            writer.println(line);
        }
        for (String line : startSection) {
            writer.println(line);
        }
        for (String line : functionsSection) {
            writer.println(line);
        }
    }

    private void recursiveGeneration(TreeNode root) {
        for (TreeNode node : root.getSons()) {
            handleTreeNode(node);
            // recursiveGeneration(node);
        }
    }

    private void handleTreeNode(TreeNode node) {
        switch (node.getLabel()) {
            case "put":
                String value = node.getSons().get(0).getLabel();
                put(value);
                break;
            case "ROOT":
                root(node);
                break;
            case "integer":
                if (node.getSons().size() > 1) {
                    variableDecl(node.getSons().get(0).getLabel(), node.getSons().get(1).getLastSon());
                } else {
                    variableDecl(node.getSons().get(0).getLabel(), null);
                }
                break;
            case "string":
                if (node.getSons().size() > 1) {
                    variableDecl(node.getSons().get(0).getLabel(), node.getSons().get(1).getLastSon());
                } else {
                    variableDecl(node.getSons().get(0).getLabel(), null);
                }
                break;
            case "boolean":
                if (node.getSons().size() > 1) {
                    variableDecl(node.getSons().get(0).getLabel(), node.getSons().get(1).getLastSon());
                } else {
                    variableDecl(node.getSons().get(0).getLabel(), null);
                }
                break;
            case "float":
                if (node.getSons().size() > 1) {
                    variableDecl(node.getSons().get(0).getLabel(), node.getSons().get(1).getLastSon());
                } else {
                    variableDecl(node.getSons().get(0).getLabel(), null);
                }
                break;
            case "character":
                if (node.getSons().size() > 1) {
                    variableDecl(node.getSons().get(0).getLabel(), node.getSons().get(1).getLastSon());
                } else {
                    variableDecl(node.getSons().get(0).getLabel(), null);
                }
                break;
            case "AFFECT":
                affectation(node.getSons().get(0).getLabel(), node.getSons().get(1));
                break;
            case "if":
                handleIf(node);
                break;
            case "while":
                handleWhile(node);
                break;
            case "function":
                handleFunction(node);
                break;
            case "for":
                forLoop(node);
                break;
            case "return":
                handleReturn(node);
                break;
            default:
                for (TreeNode son : node.getSons()) {
                    handleTreeNode(son);
                }
                break;
        }
    }

    public void root(TreeNode node) {
        dataSection.add("section .data");
        bssSection.add("section .bss");
        rodataSection.add("section .rodata");
        rodataSection.add("    int_format  db  \"%i\",0");
        rodataSection.add("    string_format db \"%s\",0");
        rodataSection.add("    float_format db \"%f\",0");
        textSection.add("section .text");
        textSection.add("    extern printf, scanf, putchar,fflush");
        textSection.add("    global  main,print_string, print_nl, print_int, read_int");
        startSection.add("main:");
    }

    public void put(String value) {
        startSection.add("  ;  DEBUT PUT  ");
        // On regarde si la valeur est une chaine de caractère ou un caractère
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("\'") && value.endsWith("\'"))) {
            value = value.replace("\"", "");
            value = value.replace("\'", "");
            String id = generateRandomString();
            rodataSection.add("    " + id + " db \"" + value + "\",0; variable " + value);
            startSection.add("    mov rdi,0; charge la valeur string dans rdi");
            startSection.add("    lea rdi,[" + id + "]; charge la valeur string dans rdi");
            startSection.add("    call print_string");
            startSection.add("    call print_nl");
        }
        // On regarde si la valeur est un charactère
        else if (value.startsWith("\'") && value.endsWith("\'")) {
            value = value.replace("\'", "");
            startSection.add("    mov rax,1; appel système pour écrire");
            startSection.add("    mov rdi,1; ; numéro de l'appel système dans rdi");
            startSection.add("    mov rcx," + value + "; message à écrire");
            startSection.add("    mov rdx,1; longueur du message");
            startSection.add("    syscall; interruption du noyau");
        }
        // On regarde si la valeur est un entier
        else if (value.matches("[0-9]+")) {
            startSection.add("    mov rdi," + value + "; charge la valeur int dans rdi");
            startSection.add("    call print_int");
            startSection.add("    call print_nl"); 
        }
        // On regarde si la valeur est un float
        else if (value.matches("[0-9]+.[0-9]+")) {
            //TODO
        }
        // On regarde si la valeur est un boolean
        else if (value.equals("true") || value.equals("false")) {
            if (value.equals("true")) {
                startSection.add("    mov rdi,1; charge la valeur bool dans rdi");
            } else {
                startSection.add("    mov rdi,0; charge la valeur bool dans rdi");
            }
            startSection.add("    call print_int");
            startSection.add("    call print_nl");
        }
        // Sinon on la valeur est une variable
        else {
            TDSItemVar functionTDS = tds.getTableFille().getVariables().get(value);
            Integer depl = functionTDS.getDeplacement();
            Integer hautPile = tds.getTableFille().getHautPile();
            Integer longueur = functionTDS.getTaille();
            if (functionTDS.getType().equals("integer")) {
                // convertir l'entier en string avant de l'afficher
                startSection.add("    mov rdi,[rsp + " + (hautPile - depl - longueur) + "]; charge la valeur int dans rdi");
                startSection.add("    call print_int");
                startSection.add("    call print_nl");
            } else if (functionTDS.getType().equals("float")) {
                //TODO
            } else if (functionTDS.getType().equals("boolean")) {
                startSection.add("    mov rdi,[rsp + " + (hautPile - depl - longueur) + "]; charge la valeur bool dans rdi");
                startSection.add("    call print_int");
                startSection.add("    call print_nl");
            } else if (functionTDS.getType().equals("character")) {
                startSection.add("    mov rdi,[rsp + " + (hautPile - depl - longueur) + "]; charge la valeur char dans rdi");
                startSection.add("    call print_string");
                startSection.add("    call print_nl");
            } else {
                
                startSection.add("    mov rdi,[rsp + " + (hautPile - depl - longueur) + "]; charge la valeur string dans rdi");
                startSection.add("    call print_string");
                startSection.add("    call print_nl");
            }
        }
        startSection.add("  ;  FIN PUT  \n");
    }

    private Boolean isFloat(TreeNode node) {
        Boolean res = false;
        for (TreeNode son : node.getSons()) {
            TDSItemVar functionTDS = tds.getTableFille().getVariables().get(son.getLabel());
            if (son.getLabel().matches("[0-9]+.[0-9]+")
                    || (functionTDS != null && functionTDS.getType().equals("float"))) {
                return true;
            }
            res = isFloat(son);
            if (res) {
                return true;
            }
        }
        return res;
    }

    private void cleanStack() {
        if (startSection.get(startSection.size() - 1).equals("    push rax")) {
            startSection.remove(startSection.size() - 1);
            popCounter++;
        }
    }

    private void handleFormula(TreeNode node, Boolean isLeft) {
        Boolean isFloatOperation = isFloat(node);
        String name = node.getLabel();
        if(node.getFirstSon()!=null && node.getFirstSon().getLabel().equals("ARGUMENT")){
            node = node.getFirstSon();
            //for each argument, calculate the value and store it in the stack
            int offset = 0;
            for (TreeNode son : node.getSons()) {
                startSection.add("    add rsp, "+offset+"; reserve de la memoire pour un argument");
                handleFormula(son, true);
                cleanStack();
                startSection.add("    sub rsp, "+offset+"; reserve de la memoire pour un entier");
                startSection.add("    sub rsp, 16; reserve de la memoire pour un entier");
                startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                offset += 16;
            }
            startSection.add("    call " + name + "; appel de la fonction");
            for (int i = 0; i < node.getSons().size(); i++) {
                startSection.add("    add rsp, 16; libère la mémoire d'un argument");
            }
            if(isLeft){
                startSection.add("    mov rax, r10");
            }else
                startSection.add("    mov r10, rax");
        }else if (node.getNbSons() > 0) {
            // parcours des fils pour voir s'il y a des flottants
            if (node.getSons().get(0).getNbSons() > 1) {
                handleFormula(node.getSons().get(0), true);
                handleFormula(node.getSons().get(1), false);
            } else {
                handleFormula(node.getSons().get(1), false);
                handleFormula(node.getSons().get(0), true);
            }
            if (!isFloatOperation) {
                if (node.getLabel().equals("+")) {
                    if (isLeft) {
                        startSection.add("    add rax,r10; additionne rax et r10");
                        startSection.add("    mov r14,rax");
                        popCounter--;
                    } else {
                        startSection.add("    add r10,rax; additionne r10 et rax");
                        if(popCounter<0){
                            startSection.add("    mov rax, r14");
                            popCounter++;
                        }
                    }
                } else if (node.getLabel().equals("-")) {
                    if (isLeft) {
                        startSection.add("    sub rax,r10; soustrait r10 de rax");
                        startSection.add("    mov r14,rax");
                        popCounter--;
                    } else {
                        startSection.add("    sub r10,rax; soustrait rax de r10");
                        startSection.add("    neg r10; negation de r10");
                        if(popCounter<0){
                            startSection.add("    mov rax, r14");
                            popCounter++;
                        }
                    }
                } else if (node.getLabel().equals("*")) {
                    if (isLeft) {
                        startSection.add("    imul rax,r10; multiplie rax par r10");
                        startSection.add("    mov r14,rax");
                        popCounter--;
                    } else {
                        startSection.add("    imul r10,rax; multiplie r10 par rax");
                        if(popCounter<0){
                            startSection.add("    mov rax, r14");
                            popCounter++;
                        }
                    }
                } else if (node.getLabel().equals("/")) {
                    if (isLeft) {
                        startSection.add("    mov rdx,0; met rdx à 0");
                        startSection.add("    idiv r10; divise rax par r10");
                        startSection.add("    mov r14,rax");
                        startSection.add("    mov r14,rax");
                        popCounter--;
                    } else {
                        startSection.add("    mov rdx,0; met rdx à 0");
                        startSection.add("    idiv r10; divise rax par r10");
                        startSection.add("    mov r10,rdx");
                        if(popCounter<0){
                            startSection.add("    mov rax, r14");
                            popCounter++;
                        }
                    }
                }else if(node.getLabel().equals("rem")){
                    // modulo
                    if (isLeft) {
                        startSection.add("    mov rdx,0; met rdx à 0");
                        startSection.add("    idiv r10; divise rax par r10");
                        startSection.add("    mov rax,rdx");
                        startSection.add("    mov r14,rax");
                        popCounter--;
                    } else {
                        startSection.add("    mov rdx,0; met rdx à 0");
                        startSection.add("    idiv r10; divise rax par r10");
                        startSection.add("    mov r10,rdx");
                        if(popCounter<0){
                            startSection.add("    mov rax, r14");
                            popCounter++;
                        }
                    }
                }
            } else {// opérations sur les flottants
                if (node.getLabel().equals("+")) {
                    if (isLeft) {
                        startSection.add("    faddp st1,st0; additionne st(0) et st(1)");
                    } else {
                        startSection.add("    faddp st0,st1; additionne st(1) et st(0)");
                    }
                } else if (node.getLabel().equals("-")) {
                    if (isLeft) {
                        startSection.add("    fsubp st1,st0; soustrait st(0) de st(1)");
                    } else {
                        startSection.add("    fsubp st0,st1; soustrait st(1) de st(0)");
                    }
                } else if (node.getLabel().equals("*")) {
                    if (isLeft) {
                        startSection.add("    fmulp st1,st0; multiplie st(0) par st(1)");
                    } else {
                        startSection.add("    fmulp st0,st1; multiplie st(1) par st(0)");
                    }
                } else if (node.getLabel().equals("/")) {
                    if (isLeft) {
                        startSection.add("    fdivp st1,st0; divise st(0) par st(1)");
                    } else {
                        startSection.add("    fdivp st0,st1; divise st(1) par st(0)");
                    }
                }

            }
        } else {
            // checking if value is in the tds
            TDSItemVar functionTDS = tds.getTableFille().getVariables().get(node.getLabel());
            // if value is in the tds
            if (functionTDS != null) {
                Integer depl = functionTDS.getDeplacement();
                Integer hautPile = tds.getTableFille().getHautPile();
                Integer longueur = functionTDS.getTaille();
                if (functionTDS.getType().equals("integer")) {
                    if (isLeft) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                    } else {
                        startSection.add("    mov r10,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans r10");
                    }
                } else if (functionTDS.getType().equals("float")) {
                    if (isLeft) {
                        startSection.add("    fld dword [rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur float dans le registre ST0");
                    } else {
                        startSection.add("    fld dword [rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur float dans le registre ST0");
                    }
                } else {
                    // if it's an integer
                    if (isLeft) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                    } else {
                        startSection.add("    mov r10,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans r10");
                    }
                    // if it's a float
                    if (isLeft) {
                        startSection.add("    fld dword [rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur float dans le registre ST0");
                    } else {
                        startSection.add("    fld dword [rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur float dans le registre ST0");
                    }

                    // TODO: if it's a string
                }
            } else { // si la valeur est pas dans une variable
                if (node.getLabel().matches("[0-9]+")) {
                    if (isLeft) {
                        startSection.add("    mov rax," + node.getLabel() + "; charge la valeur int dans rax");
                    } else {
                        startSection.add("    mov r10," + node.getLabel() + "; charge la valeur int dans r10");
                    }
                } else if (node.getLabel().matches("[0-9]+.[0-9]+")) {
                    if (isLeft) {
                        String id = generateRandomString();
                        dataSection.add("    " + id + " dd " + node.getLabel() + "; variable " + node.getLabel());
                        startSection.add("    fld dword [" + id
                                + "]; charge la valeur float dans le registre ST0, déplaçant la valeur courante dans ST1");
                    } else {
                        String id = generateRandomString();
                        dataSection.add("    " + id + " dd " + node.getLabel() + "; variable " + node.getLabel());
                        startSection.add("    fld dword [" + id + "]; charge la valeur float dans le registre ST0");
                    }
                }
            }
        }

    }

    public void variableDecl(String name, TreeNode valNode) {
        startSection.add("  ;  DEBUT DECLARATION VARIABLE  ");
        //System.out.println("DECLARATION VARIABLE " + name);
        TDSItemVar tdsELEM = tds.getTableFille().getVariables().get(name);
        String type = tdsELEM.getType().toLowerCase();
        String id = generateRandomString();
        // Dans le cas où la valeur est déclarée sans être affectée
        if (valNode == null) {
            startSection.add("    sub rsp, " + tdsELEM.getTaille() + "; reserve de la memoire");
            if (type.equals("integer")) {
                startSection.add("    mov qword [rsp], 0; stocke la valeur de 0 dans la pile");
            } else if (type.equals("float")) {
                startSection.add("    fldz; charge 0.0 dans le registre ST0");
                startSection.add("    fstp qword [rsp]; stocke la valeur du registre ST0 dans la pile");
            } else if (type.equals("boolean")) {
                startSection.add("    mov dword [rsp], 0; stocke la valeur de 0 dans la pile");
            } else if (type.equals("character")) {
                startSection.add("    mov qword [rsp], 0; stocke la valeur de 0 dans la pile");
            } else if (type.equals("string")) {
                startSection.add("    mov dword [rsp], 0; stocke la valeur de 0 dans la pile");
            }
        } else {
            String value = valNode.getLabel();
            // Dans le cas où la valeur affectée à la variable est une formule
            if (value.matches("[+\\-*/]") || value.equals("rem")) {
                startSection.add("    sub rsp, " + tdsELEM.getTaille() + "; reserve de la memoire");
                handleFormula(valNode, true);
                cleanStack();
                if (type.equals("integer")) {
                    startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                }
            } else {
                // Dans le cas où la valeur affectée à la variable est une variable inscrit dans la TDS
                TDSItemVar valueVar = tds.getTableFille().getVariables().get(value);
                TDSItemFctProc valueFct = tds.getTableFille().getFonctions().get(value);
                if (valueFct == null) {
                    valueFct = full_tds.getFonctions().get(value);
                }
                if (valueVar != null) {
                    startSection.add("    sub rsp, " + tdsELEM.getTaille() + "; reserve de la memoire");
                    Integer depl = valueVar.getDeplacement();
                    Integer hautPile = tds.getTableFille().getHautPile();
                    Integer longueur = valueVar.getTaille();
                    if (type.equals("integer")) {
                        dataSection.add("    " + id + " dd " + value + "; variable " + name);
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection.add("    sub rsp, 16; reserve de la memoire pour un entier");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("float")) {
                        dataSection.add("    " + id + " dd " + value + "; variable " + name);
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection.add("    sub rsp, 16; reserve de la memoire pour un float");
                        startSection.add("    mov rax, rax; charge la valeur int dans rax");
                        startSection.add("    fstp qword [rsp]; stocke la valeur du registre ST0 dans la pile");
                    } else if (type.equals("boolean")) {
                        dataSection.add("    " + id + " dd " + value + "; variable " + name);
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection.add("    sub rsp, 16; reserve de la memoire pour un boolean");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("character")) {
                        dataSection.add("    " + id + " db " + value + "; variable " + name);
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection.add("    sub rsp, 16; reserve de la memoire pour un charactere");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("string")) {
                        dataSection.add("    " + id + " db " + value + "; variable " + name);
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection.add("    sub rsp, " + tdsELEM.getTaille() + "; reserve de la memoire");
                    }
                }
                // Dans le cas où la valeur affectée à la variable est une variable inscrit dans la TDS   
                else if (valueFct != null) {
                    //System.out.println("Fonction appelée : " + value);
                    //On récupère le nombre de paramètre de la fonction
                    Integer nb_params = valueFct.getNbArgs();
                    //On va maintenant récupérer la valeur des différents paramètres
                    if (nb_params>0){
                        int offset = 0;
                        for (int i = 0; i < nb_params; i++) {
                            if(valNode.getFirstSon().getLabel().equals("ARGUMENT")){
                                valNode = valNode.getFirstSon();
                            }
                            startSection.add("    add rsp, "+offset+"; reserve de la memoire pour un argument");
                            handleFormula(valNode.getSons().get(i), true);
                            cleanStack();
                            startSection.add("    sub rsp, "+offset+"; reserve de la memoire pour un entier");
                            startSection.add("    sub rsp, "+valueFct.getTableFille().getArgument(i).getTaille()+"; reserve de la memoire pour un entier");
                            startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                            offset += 16;
                        }
                    }
                    startSection.add("    call " + value + "; appel de la fonction");
                    for(int i = 0; i < nb_params; i++){
                        startSection.add("    add rsp, 16; libère la mémoire d'un argument");
                    }
                    if (type.equals("integer")) {
                        startSection.add("    sub rsp, 16; reserve de la memoire pour un entier");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                    }
                }
                // Dans le cas où la valeur affectée à la variable est une valeur
                else {
                    startSection.add("    sub rsp, " + tdsELEM.getTaille() + "; reserve de la memoire");
                    value = value.replace("\"", "'");
                    if (type.equals("integer")) { 
                        dataSection.add("    " + id + " dd " + value + "; variable " + name);
                        startSection.add("    mov rax, [" + id + "]; charge la valeur int dans rax");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                    }
                    else if (type.equals("float")) {
                        startSection.add("  ;  FIN DECLARATION VARIABLE  \n");
                    } else if (type.equals("float")) {
                        dataSection.add("    " + id + " dd " + value + "; variable " + name);
                        startSection.add("    fld qword [" + id + "]; charge la valeur float dans le registre ST0");
                        startSection.add("    fstp qword [rsp]; stocke la valeur du registre ST0 dans la pile");
                    } 
                    else if (type.equals("boolean")) {
                        if (value.equals("true")) {
                            value = "255";
                        } else {
                            value = "0";
                        }
                        dataSection.add("    " + id + " dd " + value + "; variable " + name);
                        startSection.add("    mov rax, [" + id + "]; charge la valeur bool dans rax");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                    } 
                    else if (type.equals("character")) {
                        dataSection.add("    " + id + " db " + value + "; variable " + name);
                        startSection.add("    sub rsp, 16; reserve de la memoire pour un double");
                        startSection.add("    mov rax, [" + id + "]; charge la valeur char dans rax");
                        startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                        
                    }
                    else if (type.equals("string")) {
                        dataSection.add("    " + id + " db " + value + "; variable " + name);
                        int stringSize = value.length() - 2 + 1;
                        startSection.add("    lea rsi, [" + id + "]");
                        startSection.add("    mov rdi, rsp");
                        startSection.add("    mov rcx, " + stringSize);
                        startSection.add("    rep movsb");

                    }
                }
            }
        }
        startSection.add("  ;  FIN DECLARATION VARIABLE  \n");
    }

    public void affectation(String name, TreeNode valNode) {
        startSection.add("  ;  DEBUT AFFECTATION  ");
        TDSItemVar tdsELEM = tds.getTableFille().getVariables().get(name);
        String type = tdsELEM.getType().toLowerCase();
        // Dans le cas où on affecte une formule à une variable
        if (valNode.getLabel().matches("[+\\-*/=]")|| valNode.getLabel().equals("rem")) {
            handleFormula(valNode, true);
            cleanStack();
            if (type.equals("integer")) {
                startSection.add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                        - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
            }
        } else {
            // Dans le cas où on affecte une variable à une autre variable
            TDSItemVar valueVar = tds.getTableFille().getVariables().get(valNode.getLabel());
            TDSItemFctProc valueFct = tds.getTableFille().getFonctions().get(valNode.getLabel());
            if(valueVar == null){
                valueVar = backup_tds.getTableFille().getVariables().get(valNode.getLabel());
            }
            if(valueFct == null){
                valueFct = backup_tds.getTableFille().getFonctions().get(valNode.getLabel());
            }
            if(valueFct != null){
                Integer nb_params = valueFct.getNbArgs();
                String nomFonction = valNode.getLabel();
                    //On va maintenant récupérer la valeur des différents paramètres
                    if (nb_params>0){
                        int offset = 0;
                        for (int i = 0; i < nb_params; i++) {
                            if(valNode.getFirstSon().getLabel().equals("ARGUMENT")){
                                valNode = valNode.getFirstSon();
                            }
                            startSection.add("    add rsp, "+offset+"; décalage pile");
                            handleFormula(valNode.getSons().get(i), true);
                            cleanStack();
                            startSection.add("    sub rsp, "+offset+"; décalage pile");
                            startSection.add("    sub rsp, "+valueFct.getTableFille().getArgument(i).getTaille()+"; reserve de la memoire pour un entier");
                            startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
                            offset += 16;
                        }
                    }
                    startSection.add("    call " + nomFonction + "; appel de la fonction");
                    for(int i = 0; i < nb_params; i++){
                        startSection.add("    add rsp, 16; libère la mémoire d'un argument");
                    }
                    if (type.equals("integer")) {
                        startSection.add("    mov qword [rsp+"+(tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                        - tdsELEM.getTaille())+"], rax; stocke la valeur de rax dans la pile");
                    }
            }
            else if (valueVar != null) {
                Integer depl = valueVar.getDeplacement();
                Integer hautPile = tds.getTableFille().getHautPile();
                Integer longueur = valueVar.getTaille();
                if (valueVar.getType().equals("integer")) {
                    if (type.equals("integer")) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection
                                .add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                                        - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("float")) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection
                                .add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                                        - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("boolean")) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection
                                .add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                                        - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("character")) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection
                                .add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                                        - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                    } else if (type.equals("string")) {
                        startSection.add("    mov rax,[rsp + " + (hautPile - depl - longueur)
                                + "]; charge la valeur int dans rax");
                        startSection
                                .add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                                        - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                    }
                }
            } else {
                // Dans le cas où on affecte une valeur à une variable
                String value = valNode.getLabel();
                if (type.equals("integer")) {
                    startSection.add("    mov rax," + value + "; charge la valeur int dans rax");
                    startSection.add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                            - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                } else if (type.equals("float")) {
                    startSection.add("    mov rax," + value + "; charge la valeur float dans rax");
                    startSection.add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                            - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                } else if (type.equals("boolean")) {
                    if (value.equals("true")) {
                        value = "255";
                    } else {
                        value = "0";
                    }
                    startSection.add("    mov rax," + value + "; charge la valeur bool dans rax");
                    startSection.add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                            - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                } else if (type.equals("character")) {
                    startSection.add("    mov rax," + value + "; charge la valeur char dans rax");
                    startSection.add("    mov [rsp + " + (tds.getTableFille().getHautPile() - tdsELEM.getDeplacement()
                            - tdsELEM.getTaille()) + "], rax; stocke la valeur de rax dans la pile");
                } else if (type.equals("string")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.replace("\"", "");
                        String id = generateRandomString();
                        dataSection.add("    " + id + " db '" + value + "', 0x0a; message + saut de ligne");
                        dataSection.add("    " + id + "_len equ $-" + id + "; longueur du message");
                        startSection.add("    lea rsi, [" + id + "]");
                        startSection.add("    mov rdi, rsp");
                        startSection.add("    mov rcx, " + value.length() + "; longueur du message");
                        startSection.add("    rep movsb");
                    }
                }
            }
        }
        startSection.add("  ;  FIN AFFECTATION  \n");
    }
 
    private void handleIf(TreeNode node) {
        String if_id = generateRandomString();
        startSection.add("  ;  DEBUT IF  ");
        TreeNode condition = node.getSons().get(0); // correspond à la condition
        handleComplexCondition(condition, "if_else_"+if_id, "if_"+if_id);
        boolean hasElse = false;
        // now handling body
        startSection.add("if_" + if_id + ":");
        ArrayList<Integer> toRemove = new ArrayList<>();
        for (int i = 1; i < node.getNbSons(); i++) {
            if (!hasElse && node.getSons().get(i).getLabel().equals("else") && !toRemove.contains(i)) {
                startSection.add("    jmp if_end_" + if_id);
                startSection.add("if_else_" + if_id + ":");
                hasElse = true;
                handleTreeNode(node.getSons().get(i));
                startSection.add("    jmp if_end_" + if_id);
            } else if (!hasElse && node.getSons().get(i).getLabel().equals("elsif") && !toRemove.contains(i)) {
                startSection.add("    jmp if_end_" + if_id);
                startSection.add("if_else_" + if_id + ":");
                hasElse = true;
                for (int j = i + 1; j < node.getNbSons(); j++) {
                    if (node.getSons().get(j).getLabel().equals("else")
                            || node.getSons().get(j).getLabel().equals("elsif")) {
                        node.getSons().get(i).addSon(node.getSons().get(j));
                        toRemove.add(j);
                    }
                }
                handleIf(node.getSons().get(i));
            } else {
                if(!toRemove.contains(i))
                handleTreeNode(node.getSons().get(i));
            }

        }
        // fin du if, retour au contexte précédent
        if(!hasElse){
            startSection.add("if_else_" + if_id + ":");
            startSection.add("    jmp if_end_" + if_id);
        }
        startSection.add("if_end_" + if_id + ":");
        startSection.add("  ;  FIN IF  \n");
    }

    private void handleWhile(TreeNode node){
        String while_id = generateRandomString();
        startSection.add("  ;  DEBUT WHILE  ");
        TreeNode condition = node.getSons().get(0); // correspond à la condition
        if(condition.getLabel().equals("true")){
            startSection.add("while_"+while_id+":");
        }else if(condition.getLabel().equals("false")){
            startSection.add("    jmp while_end_"+while_id);
        }else if (condition.getLabel().equals("=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                startSection.add("while_"+while_id+":");
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jne while_end_"+while_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals("/=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                startSection.add("while_"+while_id+":");
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    je while_end_"+while_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals("<")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                startSection.add("while_"+while_id+":");
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jge while_end_"+while_id);
            }else{
                // TODO float equality
            }
        } else if(condition.getLabel().equals(">")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                startSection.add("while_"+while_id+":");
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jle while_end_"+while_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals("<=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                startSection.add("while_"+while_id+":");
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jg while_end_"+while_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals(">=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                startSection.add("while_"+while_id+":");
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jl while_end_"+while_id);
            }else{
                // TODO float equality
            }
        }

        // now handling body
        for (int i = 1; i < node.getNbSons(); i++) {
                handleTreeNode(node.getSons().get(i));
        }
        startSection.add("    jmp while_"+while_id);
        startSection.add("while_end_"+while_id+":");
        startSection.add("  ;  FIN WHILE  \n");
    }

    private void handleSimpleCondition(TreeNode node, String end_id){
        TreeNode condition = node; // correspond à la condition
        if(condition.getLabel().equals("true")){
        }else if(condition.getLabel().equals("false")){
            startSection.add("    jmp "+end_id);
        }else if (condition.getLabel().equals("=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jne "+end_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals("/=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    je "+end_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals("<")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jge "+end_id);
            }else{
                // TODO float equality
            }
        } else if(condition.getLabel().equals(">")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jle "+end_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals("<=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jg "+end_id);
            }else{
                // TODO float equality
            }
        }else if(condition.getLabel().equals(">=")&& condition.getNbSons() == 2){
            Boolean isFloat = isFloat(condition);
            if(!isFloat){
                handleFormula(condition.getSons().get(0), true);
                cleanStack();
                startSection.add("    mov rcx, rax");
                handleFormula(condition.getSons().get(1), true);
                cleanStack();
                startSection.add("    cmp rcx, rax");
                startSection.add("    jl "+end_id);
            }else{
                // TODO float equality
            }
        }
    }

    private void handleComplexCondition(TreeNode node, String end_id, String start_id){
        TreeNode condition = node; // correspond à la condition
        if(condition.getLabel().equals("and")){
            String new_start_id = generateRandomString();
            handleComplexCondition(condition.getSons().get(0), end_id, new_start_id);
            startSection.add("    jmp "+new_start_id+"; saut à la condition suivante");
            startSection.add(new_start_id+":");
            handleComplexCondition(condition.getSons().get(1), end_id, start_id);
        }else if (condition.getLabel().equals("or")){
            handleComplexCondition(condition.getSons().get(0), end_id, start_id);
            startSection.add("    jmp "+start_id);
            handleComplexCondition(condition.getSons().get(1), end_id, start_id);
        }else{
            handleSimpleCondition(node, end_id);
            startSection.add("    jmp "+start_id);
        }    
    }

    private void forLoop(TreeNode node){
        String for_id = generateRandomString();
        if(node.getNbSons()>=2){
            startSection.add("; DEBUT BOUCLE FOR ;");
            //initialisation de la variable de boucle
            handleTreeNode(node.getSons().get(0));
            handleFormula(node.getSons().get(1).getSons().get(0), true);
            cleanStack();
            startSection.add("    mov qword [rsp], rax; stocke la valeur de rax dans la pile");
            startSection.add("for_"+for_id+":");
            //condition de la boucle
            handleFormula(node.getSons().get(1).getSons().get(1), true);
            cleanStack();
            startSection.add("    mov rcx, rax");
            startSection.add("    cmp [rsp], rcx");
            startSection.add("    jg for_end_"+for_id);
            //corps de la boucle
            for (int i = 2; i < node.getNbSons(); i++) {
                handleTreeNode(node.getSons().get(i));
            }
            //incrémentation de la variable de boucle
            startSection.add("    mov rax, [rsp] ; récupération de la variable de boucle");
            startSection.add("    add rax, 1 ; incrémentation de la variable de boucle");
            startSection.add("    mov [rsp], rax ; sauvegarde de la variable de boucle");
            startSection.add("    jmp for_"+for_id);
            startSection.add("for_end_"+for_id+":");
            //suppression de la variable de boucle
            startSection.add("    add rsp, 16; suppression de la variable de boucle");
            tds.getTableFille().getVariables().remove(node.getSons().get(0).getLabel());
        }
    }

    private void handleFunction(TreeNode node){
        TDSItemFctProc tdsELEM = full_tds.getFonctions().values().iterator().next().getTableFille().getFonctions().get(node.getFirstSon().getLabel());
        this.backup_tds = this.tds;
        this.tds = tdsELEM;
        String function_name = node.getFirstSon().getLabel();
        String backup = textSection.get(textSection.size()-1);
        textSection.remove(textSection.size()-1);
        textSection.add(backup + ", " + function_name);
        startSection.add("  ;  DEBUT FUNCTION  ");
        startSection.add(function_name+":");
                //sauvegarde du pointeur de pile
                startSection.add("    push rbp; sauvegarde du pointeur de pile");
                //récupération des paramètres
                startSection.add("    mov rbp, rsp; sauvegarde du pointeur de pile");
                //pour chaque paramètre, on remonte dans la pile les valeurs
                for (int i = tdsELEM.getNbArgs()-1; i>=0; i--) {
                    startSection.add("    mov rax, [rbp + "+(16 + 16*i)+"]; récupération du paramètre "+i);
                    startSection.add("    sub rsp, 16; reserve de la memoire pour un entier");
                    startSection.add("    mov [rsp], rax; stockage du paramètre "+i);
                }
                //code interne
                for (int i = 3; i < node.getNbSons(); i++) {
                    handleTreeNode(node.getSons().get(i));
                }
                //retour de la fonction
                startSection.add("    mov rsp, rbp; restauration du pointeur de pile");
                startSection.add("    leave; restauration du pointeur de pile");
                startSection.add("    ret; retour de la fonction");
        startSection.add("  ;  FIN FUNCTION  \n");
        // now moving everything to the function section
        Boolean status = false;
        ArrayList<Integer> toRemove = new ArrayList<>();
        for(int i=0; i<startSection.size(); i++){
            if(startSection.get(i).contains("DEBUT FUNCTION")){
                status = true;
            }
            if(status){
                functionsSection.add(startSection.get(i));
                toRemove.add(i);
            }
            if(startSection.get(i).contains("FIN FUNCTION")){
                status = false;
            }
        }
        for(int i=toRemove.size()-1; i>=0; i--){
            startSection.remove((int) toRemove.get(i));
        }
        //resetting the TDS
        this.tds = this.backup_tds;
    }

    private void handleReturn(TreeNode node){
        if(node.getNbSons() == 1){
            handleFormula(node.getSons().get(0), true);
            cleanStack();
            startSection.add("    mov rax, rax; charge la valeur int dans rax");
        }
    }
}
