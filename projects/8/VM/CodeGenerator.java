import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Stack;

public class CodeGenerator {

    private FileWriter writer;
    private File asmFile;
    private String currentVMFile;
    private Hashtable<String,String> segmentTable = new Hashtable<>();
    private String fileName;
    private int labelCounter = 0;
    private Stack<String> funcStack = new Stack<>();

    public CodeGenerator(String fileName) throws IOException {
        this.fileName = fileName.replace("vm", "asm");
        this.asmFile = new File(fileName.replace("vm", "asm"));
        this.writer = new FileWriter(asmFile);
        segmentTable.put("local", "LCL");
        segmentTable.put("argument", "ARG");
        segmentTable.put("this", "THIS");
        segmentTable.put("that", "THAT");
    }

    public CodeGenerator(String fileName, boolean bootsTrap) throws IOException {
        this.fileName = fileName;
        this.asmFile = new File(fileName);
        this.writer = new FileWriter(asmFile);
        segmentTable.put("local", "LCL");
        segmentTable.put("argument", "ARG");
        segmentTable.put("this", "THIS");
        segmentTable.put("that", "THAT");
        insertBootstrapping();
    }

    public void writeLabel(String label) throws IOException {
        String finalLabel = funcStack.isEmpty() ? label : funcStack.peek() + "$" + label;
        writer.write("(" + finalLabel + ")\n");
    }

    public void writeGoto(String label) throws IOException {
        String finalLabel = funcStack.isEmpty() ? label : funcStack.peek() + "$" + label;
        writer.write("@" + finalLabel +"\n");
        writer.write("0;JMP\n");
    }

    public void writeIf(String label) throws IOException {
        String finalLabel = funcStack.isEmpty() ? label : funcStack.peek() + "$" + label;
        popToD();
        writer.write("@" + finalLabel + "\n");
        writer.write("D;JNE\n");
    }

    public void writeFunction(String functionName, int numLocals) throws IOException {
        funcStack.push(functionName);
        writer.write("(" + functionName + ")\n");
        for (int i = 0; i < numLocals; i++) {
            writer.write("@0\n");
            writer.write("D=A\n");
            loadSP();
            writer.write("M=D\n");
            incrementSP();
        }
    }

    public void writeCall(String functionName, int numArgs) throws IOException {
        String funcContext = funcStack.isEmpty() ? "Sys" : funcStack.peek();
        String returnLabel = funcContext + "$ret." + labelCounter;
        labelCounter++;

        //push return address
        writer.write("@" + returnLabel + "\n");
        writer.write("D=A\n");
        pushD();

        pushSegment("LCL");
        pushSegment("ARG");
        pushSegment("THIS");
        pushSegment("THAT");

        //ARG = SP - 5 - numArgs
        writer.write("@SP\n");
        writer.write("D=M\n");
        writer.write("@" + (5 + numArgs) + "\n");
        writer.write("D=D-A\n");
        writer.write("@ARG\n");
        writer.write("M=D\n");

        //LCL = SP
        writer.write("@SP\n");
        writer.write("D=M\n");
        writer.write("@LCL\n");
        writer.write("M=D\n");

        //goto func & return address label setzen 
        writer.write("@" + functionName + "\n");
        writer.write("0;JMP\n");
        writer.write("(" + returnLabel + ")\n");
    }

    public void writeReturn() throws IOException{
        //Frame = LCL
        writer.write("@LCL\n");
        writer.write("D=M\n");
        writer.write("@R13\n");
        writer.write("M=D\n");

        //retAddr = *(frame-5)
        writer.write("@5\n");
        writer.write("A=D-A\n");
        writer.write("D=M\n");
        writer.write("@R14\n");
        writer.write("M=D\n");

        //*ARG = pop
        writer.write("@SP\n");
        writer.write("AM=M-1\n");
        writer.write("D=M\n");
        writer.write("@ARG\n");
        writer.write("A=M\n");
        writer.write("M=D\n");

        //SP=ARG+1
        writer.write("@ARG\n");
        writer.write("D=M+1\n");
        writer.write("@SP\n");
        writer.write("M=D\n");

        //THAT = *(frame-1)
        restoreFrame("THAT");

        //THIS = *(frame-2)
        restoreFrame("THIS");

        //ARG = *(frame-3)
        restoreFrame("ARG");

        //LCL = *(frame-4)
        restoreFrame("LCL");

        //goto retAddr
        writer.write("@R14\n");
        writer.write("A=M\n");
        writer.write("0;JMP\n");
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
            case "constant": 
                pushConstant(index);
                break;
            case "pointer":
                pushPointer(index);
                break;
            case "temp":
                pushTemp(index);
                break;
            case "static":
                pushStatic(index);
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
                popConstant(index);
                break;
            case "pointer":
                popPointer(index);
                break;
            case "temp":
                popTemp(index);
                break;
            case "static":
                popStatic(index);
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

    // Wert in D speichern
    private void pushConstant(int index) throws IOException {
        writer.write("@" + index + "\n");
        writer.write("D=A\n");
    }

    private void pushPointer(int index) throws IOException {
        writer.write("@R" + String.valueOf(3 + index) + "\n");
        writer.write("D=M\n");
    }

    private void pushTemp(int index) throws IOException {
        writer.write("@R" + String.valueOf(5 + index) + "\n");
        writer.write("D=M\n");
    }

    // Wert als filename.index speichern, "/" im filename durch "." ersetzen
    private void pushStatic(int index) throws IOException {
        writer.write("@" + currentVMFile + "." + String.valueOf(index) + "\n");
        writer.write("D=M\n");
    }

    private void popConstant(int index) throws IOException {
        writer.write("@" + index + "\n");
    } 

    private void popPointer(int index) throws IOException {
        writer.write("@R" + String.valueOf(3 + index) + "\n");
    }

    private void popTemp(int index) throws IOException {
        writer.write("@R" + String.valueOf(5 + index) + "\n");
    }

    private void popStatic(int index) throws IOException {
        writer.write("@" + currentVMFile + "." + String.valueOf(index) + "\n");
    }

    private void pushSegment(String segment) throws IOException {
        writer.write("@" + segment + "\n");
        writer.write("D=M\n");
        pushD();
    }

    private void restoreFrame(String segment) throws IOException {
        writer.write("@R13\n");
        writer.write("AM=M-1\n");
        writer.write("D=M\n");
        writer.write("@" + segment + "\n");
        writer.write("M=D\n");
    }

    private void insertBootstrapping() throws IOException {
        writer.write("@256\n");
        writer.write("D=A\n");
        writer.write("@SP\n");
        writer.write("M=D\n");
        writeCall("Sys.init", 0);
    }

    public void setCurrentVMFile(String vmFileName) {
        this.currentVMFile = vmFileName.split("\\.")[0].replaceAll("/", "\\.");
    }
}