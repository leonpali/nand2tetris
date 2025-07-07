import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class VMWriter {

    private FileWriter writer;
    private String fileName;

    public VMWriter(File file) throws IOException {
        File vmFile = new File(file.getParent(), file.getName().replace(".jack", ".vm"));
        this.writer = new FileWriter(vmFile);
        this.fileName = file.getName().replace(".jack", "");
    }

    public void writePush(String segment, int index) throws IOException {
        writer.write("push " + segment + " " + index + "\n");
    }

    public void writePop(String segment, int index) throws IOException {
        writer.write("pop " + segment + " " + index + "\n");
    }

    public void writeArithmetic(String command) throws IOException {
        writer.write(command + "\n");
    }

    public void writeCall(String name, int nArgs) throws IOException {
        writer.write("call " + name + " " + nArgs + "\n");
    }

    public void writeSubroutine(String subroutineName, int nLocals) throws IOException {
        writer.write("function " + fileName + "." + subroutineName + " " + nLocals + "\n");
    }

    public void writeReturn() throws IOException {
        writer.write("return\n");
    }

    public void writeLabel(String label) throws IOException {
        writer.write("label " + label + "\n");
    }

    public void writeIf(String label) throws IOException {
        writer.write("if-goto " + label + "\n");
    }

    public void writeGoto(String label) throws IOException {
        writer.write("goto " + label + "\n");
    }

    public void close() throws IOException {
        writer.close();
    }
}