package com.archko.map.baidu_map;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * @author archko
 */
public class City implements Serializable {

    private static final long serialVersionUID = 1L;
    public String name;
    public String id;
    public String letter;
    public String pinyin;

    public City() {
    }

    public void parseFirstLetter() {
        if (!TextUtils.isEmpty(pinyin)) {
            letter = pinyin.substring(0, 1).toUpperCase();
        }
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", letter='" + letter + '\'' +
                ", pinyin='" + pinyin + '\'' +
                '}';
    }
}