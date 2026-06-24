package org.example;

public class Main {
    public static void main(String[] args) {
        int first;
        try {
            first = Integer.parseInt(args[0]);
        } catch (Exception e) {
            first = Integer.MIN_VALUE;
        }
        System.out.println("First number: " + first);
        int second;
        try {
            second = Integer.parseInt(args[1]);
        } catch (Exception e) {
            try {
                second = Integer.parseInt(System.getenv("SECOND_NUMBER"));
            } catch (Exception e2) {
                second = Integer.MAX_VALUE;
            }
        }
        System.out.println("Second number: " + second);
        System.out.println("Your calculation: " + Calculator.add(first, second));
    }
}