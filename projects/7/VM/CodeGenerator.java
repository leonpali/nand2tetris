import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

public class CodeGenerator {

    private FileWriter writer;
    private File asmFile;
    private Hashtable<String,String> segmentTable = new Hashtable<>();
    private String fileName;
    private int labelCounter = 0;

    public CodeGenerator(String fileName) throws IOException {
        this.fileName = fileName.replace("vm", "asm");
        this.asmFile = new File(fileName.replace("vm", "asm"));
        this.writer = new FileWriter(asmFile);
        segmentTable.put("local", "LCL");
        segmentTable.put("argument", "ARG");
        segmentTable.put("this", "THIS");
        segmentTable.put("that", "THAT");
    }    

    public void writeArithmetic(String command) throws IOException {
        switch (command) {
            case "add":
                add();
                break;
            case "sub":
                sub();
                break;
            case "neg":
                neg();
                break;
            case "eq":
                writeComparison("JEQ");
                break;
            case "gt":
                writeComparison("JGT");
                break;
            case "lt":
                writeComparison("JLT");
                break;
            case "and":
                and();
                break;
            case "or":
                or();
                break;
            case "not":
                not();
                break;
        }
    } 

    public void writePushPop(String command, String segment, int index) throws IOException {
        if(command.equals("C_PUSH")) {
            push(segment, index);
        } else {
            pop(segment, index);
        }
    }

    public void close() throws IOException {
        writer.close();
    }

    private void push(String segment, int index) throws IOException {
        switch (segment) {
            // Wert in D speichern
            case "constant": 
                writer.write("@" + index + "\n");
                writer.write("D=A\n");
                break;
            case "pointer":
                writer.write("@R" + String.valueOf(3 + index) + "\n");
                writer.write("D=M\n");
                break;
            case "temp":
                writer.write("@R" + String.valueOf(5 + index) + "\n");
                writer.write("D=M\n");
                break;
            // Wert als filename.index speichern
            case "static":
                writer.write("@" + fileName.split("\\.")[0] + "." + String.valueOf(index) + "\n");
                writer.write("D=M\n");
                break;
            default:
                loadSegment(segment, index);
                writer.write("D=M\n");
        }
        pushD();
    }

    private void pop(String segment, int index) throws IOException {
        switch (segment) {
            case "constant":
                writer.write("@" + index + "\n");
                break;
            case "pointer":
                writer.write("@R" + String.valueOf(3 + index) + "\n");
                break;
            case "temp":
                writer.write("@R" + String.valueOf(5 + index) + "\n");
                break;
            case "static":
                writer.write("@" + fileName.split("\\.")[0] + "." + String.valueOf(index) + "\n");
                break;
            default:
                loadSegment(segment, index);
        }
        writer.write("D=A\n");
        writer.write("@R13\n");
        writer.write("M=D\n");
        popToD();
        writer.write("@R13\n");
        writer.write("A=M\n");
        writer.write("M=D\n");
    }

    private void loadSegment(String segment, int index) throws IOException {
        writer.write("@" + segmentTable.get(segment) + "\n");
        writer.write("D=M\n");
        writer.write("@" + String.valueOf(index) + "\n");
        writer.write("A=D+A\n");
    }

    private void loadSP() throws IOException {
        writer.write("@SP\n");
        writer.write("A=M\n");
    }

    private void pushD() throws IOException {
        loadSP();
        writer.write("M=D\n");
        incrementSP();
    }

    private void popToD() throws IOException {
        decrementSP();
        writer.write("A=M\n");
        writer.write("D=M\n");
    }

    private void incrementSP() throws IOException {
        writer.write("@SP\n");
        writer.write("M=M+1\n");
    }

    private void decrementSP() throws IOException {
        writer.write("@SP\n");
        writer.write("M=M-1\n");
    }

    private void add() throws IOException {
        popToD();
        writer.write("@SP\n");
        writer.write("M=M-1\n");
        writer.write("A=M\n");
        writer.write("M=D+M\n");
        incrementSP();
    }

    private void sub() throws IOException {
        popToD();
        writer.write("@SP\n");
        writer.write("M=M-1\n");
        writer.write("A=M\n");
        writer.write("M=M-D\n");
        incrementSP();
    }

    private void neg() throws IOException {
        writer.write("@SP\n");
        writer.write("A=M-1\n");
        writer.write("M=-M\n");
    }

    private void writeComparison(String jmpCommand) throws IOException {
        popToD();
        decrementSP();
        loadSP();
        writer.write("D=M-D\n");
        writer.write("@TRUE" + labelCounter + "\n");
        writer.write("D;" + jmpCommand + "\n");
        loadSP();
        writer.write("M=0\n");
        writer.write("@END" + labelCounter + "\n");
        writer.write("0;JMP\n");
        writer.write("(TRUE" + labelCounter + ")\n");
        loadSP();
        writer.write("M=-1\n");
        writer.write("(END" + labelCounter + ")\n");
        incrementSP();
        labelCounter++;
    }

    private void and() throws IOException {
        popToD();
        writer.write("@SP\n");
        writer.write("M=M-1\n");
        writer.write("A=M\n");
        writer.write("M=D&M\n");
        incrementSP();
    }

    private void or() throws IOException {
        popToD();
        writer.write("@SP\n");
        writer.write("M=M-1\n");
        writer.write("A=M\n");
        writer.write("M=M|D\n");
        incrementSP();
    }

    private void not() throws IOException {
        writer.write("@SP\n");
        writer.write("A=M-1\n");
        writer.write("M=!M\n");
    }
}