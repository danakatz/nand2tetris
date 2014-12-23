import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by danakatz on 11/12/14.
 */
public class JackCompiler {
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                ArrayList<File> files = new ArrayList<File>();

                File input = new File(args[0]);
                getFiles(input, files);

                if (!files.isEmpty()) {
                    for (File f : files) {
                        String outputName = f.getName();
                        outputName = outputName.substring(0, outputName.indexOf('.'));
                        File output = new File(f.getParent(), outputName + ".vm");

                        CompilationEngine ce = new CompilationEngine(f, output);
                        ce.compileClass();
                        ce.close();
                    }
                } else {
                    System.out.println("No .jack files found.");
                }
            } catch(Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No source entered.");
        }
    }

    private static void getFiles(File input, ArrayList<File> files) throws FileNotFoundException {
        if(input.isFile()) {
            // check for .vm extension before adding to list of files
            String filename = input.getName();
            int extension = filename.indexOf('.');
            if(extension > 0) {
                String fileExtension = filename.substring(extension + 1);
                if(fileExtension.equalsIgnoreCase("jack")) {
                    files.add(input);
                }
            }
        } else if(input.isDirectory()) {
            File[] innerFiles = input.listFiles();
            for(File f : innerFiles) {
                getFiles(f, files);
            }
        } else {
            throw new FileNotFoundException("Could not find file or directory.");
        }
    }
}
