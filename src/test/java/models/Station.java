package models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Station {
    private final String number;
    private final String name;
    private final String color;
}