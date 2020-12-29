package com.example.lib;

public class MyClass {
    public static void main(String[] args) {
        System.out.println(Direction.EAST.getAngle());

    }

    enum Direction {
        EAST(0), NORTH(90), WEST(180), SOUTH(360);

        Direction(final int angle) {
            this.angle = angle;
        }
        private int angle;

        int getAngle() {
            return angle;
        }


    }
}