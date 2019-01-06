package com.yuyakaido.android.cardstackview.sample;

public class Spot {
    private static int counter = 0;

    public long id;
    public String name;
    public String body;
    public String url;

    public Spot(String name, String body, String url) {
        this.id = counter++;
        this.name = name;
        this.body = body;
        this.url = url;
    }
//    public Spot(String url) {
//        this.id = counter++;
//
//        this.url = url;
//    }
}
