package screencapture;

import org.bytedeco.javacpp.opencv_core;

import java.util.ArrayList;

public abstract class Analyzer {

    private String name;
    private String value;
    private String previousValue;
    private ArrayList<Tuple<String, String>> dependencyStrings;
    private ArrayList<Tuple<Analyzer, String>> dependencies;

    public Analyzer(String name){
        this.name = name;
        this.value = "";
        this.previousValue = "";
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

    public String getPreviousValue() {
        return previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
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
