package com.example.weatherapp_v2.model;

public class CityList {
    private int id;
    private String Name;
    private String Contry;
    private Coord coord;

    public CityList() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getContry() {
        return Contry;
    }

    public void setContry(String contry) {
        Contry = contry;
    }

    public Coord getCoord() {
        return coord;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }
}
