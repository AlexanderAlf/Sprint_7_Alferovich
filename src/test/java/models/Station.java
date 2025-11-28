package models;

public class Station {
    private final String number;
    private final String name;
    private final String color;

    public Station(String number, String name, String color) {
        this.number = number;
        this.name = name;
        this.color = color;
    }

    public String getNumber() { return number; }
    public String getName() { return name; }
    public String getColor() { return color; }
}