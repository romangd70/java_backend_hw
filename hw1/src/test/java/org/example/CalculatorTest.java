package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorTest {

    @Test
    void addReturnsSumOfTwoNumbers() {
        assertEquals(7, Calculator.add(3, 4));
        assertEquals(-1, Calculator.add(3, -4));
    }

    @Test
    @SuppressWarnings("deprecation")
    void addOldReturnsSumOfTwoNumbers() {
        assertEquals(10, Calculator.addOld(6, 4));
    }

    @Test
    void subtractReturnsDifferenceOfTwoNumbers() {
        assertEquals(5, Calculator.subtract(9, 4));
        assertEquals(-13, Calculator.subtract(-9, 4));
    }

    @Test
    void multiplyReturnsProductOfTwoNumbers() {
        assertEquals(20, Calculator.multiply(5, 4));
        assertEquals(0, Calculator.multiply(5, 0));
    }

    @Test
    void divideReturnsDivisionResult() {
        assertEquals(2.5, Calculator.divide(5, 2));
        assertEquals(-3.0, Calculator.divide(9, -3));
    }

    @Test
    void divideByZeroThrowsArithmeticException() {
        assertThrows(ArithmeticException.class, () -> Calculator.divide(5, 0));
    }

    @Test
    void factorialReturnsFactorialForPositiveNumber() {
        assertEquals(120, Calculator.factorial(5));
    }

    @Test
    void factorialReturnsOneForZeroAndOne() {
        assertEquals(1, Calculator.factorial(0));
        assertEquals(1, Calculator.factorial(1));
    }

    @Test
    void factorialForNegativeNumberThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Calculator.factorial(-1));
    }
}
