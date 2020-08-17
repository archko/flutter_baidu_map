package com.archko.map.baidu_map;

import java.io.Serializable;

/**
 * @author archko
 */
public class City implements Serializable {

    private static final long serialVersionUID = 1L;
    public String name;
    public String id;
    public String letter;

    public City() {
    }

    public City(String id, String name, String letter) {
        this.id = id;
        this.name = name;
        this.letter = letter;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", letter='" + letter + '\'' +
                '}';
    }
}