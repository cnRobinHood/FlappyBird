package com.lc.flappybird.domain;

import org.litepal.crud.LitePalSupport;

public class Pipe extends LitePalSupport {

    private float positionX;

    private float height;

    public Pipe(float positionX, float height) {
        this.positionX = positionX;
        this.height = height;
    }

    public float getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
