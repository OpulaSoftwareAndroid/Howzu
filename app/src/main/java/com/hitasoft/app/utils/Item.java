package com.hitasoft.app.utils;

public class Item {
    private int id;
    private String name;
    private String image;

    public Item(int id, String name,String image) {
        this.id = id;
        this.name = name;
        this.image = image;

    }

    public void setId(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

}
