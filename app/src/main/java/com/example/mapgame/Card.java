package com.example.mapgame;

public class Card {
    private int imageResId;
    private boolean isMatched;
    private boolean isFaceUp;

    public Card(int imageResId) {
        this.imageResId = imageResId;
        this.isMatched = false;
        this.isFaceUp = false;
    }

    public int getImageResId() {
        return imageResId;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
    }

    public boolean isFaceUp() {
        return isFaceUp;
    }

    public void setFaceUp(boolean faceUp) {
        isFaceUp = faceUp;
    }
}
