package screencapture;

import org.bytedeco.javacpp.opencv_core;

import java.util.ArrayList;

public abstract class Analyzer {

    private String name;
    private String value;
    private ArrayList<Tuple<String, String>> dependencyStrings;
    private ArrayList<Tuple<Analyzer, String>> dependencies;
    private Logger logger;
    private Communicator communicator;

    public Analyzer(String name, Logger logger, Communicator communicator){
        this.name = name;
        this.logger = logger;
        this.communicator = communicator;
        this.value = "";
        dependencyStrings = new ArrayList<>();
        dependencies = new ArrayList<>();
    }

    public String getName() {
        return name;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Logger getLogger() {
        return logger;
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    public ArrayList<Tuple<String, String>> getDependencyStrings() {
        return dependencyStrings;
    }

    public void setDependencyStrings(ArrayList<Tuple<String, String>> dependencyStrings) {
        this.dependencyStrings = dependencyStrings;
    }

    public ArrayList<Tuple<Analyzer, String>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Tuple<Analyzer, String> dependencies) {
        this.dependencies.add(dependencies);
    }

    public String toString(){
        return "{\"name\": \"" + name + "\", \"value\": \"" + value + "\"}";
    }
    public abstract void processImage(opencv_core.IplImage image);
}
