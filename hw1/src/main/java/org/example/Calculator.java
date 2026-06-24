package org.example;

/**
 * <a href="https://starkovden.github.io/Javadoc-tags.html">Шпаргалка</a>
 * <p>
 *
 * <table border="1">
 *   <tr>
 *     <td> cell 11 </td> <td> cell 21</td>
 *   </tr>
 *   <tr>
 *     <td> cell 12 </td> <td> cell 22</td>
 *   </tr>
 * </table>
 *
 * Класс {@code Calculator} предоставляет базовые арифметические операции.
 * Все методы класса являются статическими.
 *
 * @author Иван Иванов
 * @version 1.0, 16 Янв 2026
 */
public class Calculator {

    /**
     * Складывает два целых числа.
     *
     * @param a первое слагаемое
     * @param b второе слагаемое
     * @return сумма чисел {@code a} и {@code b}
     */
    public static int add(int a, int b) {
        return a + b;
    }
    /**
     * @deprecated
     * Складывает два целых числа.
     *
     * @param a первое слагаемое
     * @param b второе слагаемое
     * @return сумма чисел {@code a} и {@code b}
     */
    @Deprecated
    public static int addOld(int a, int b) {
        return a + b;
    }

    /**
     * Вычитает одно число из другого.
     *
     * @param a уменьшаемое
     * @param b вычитаемое
     * @return результат вычитания {@code b} из {@code a}
     */
    public static int subtract(int a, int b) {
        return a - b;
    }

    /**
     * Умножает два целых числа.
     *
     * @param a первый множитель
     * @param b второй множитель
     * @return произведение {@code a} и {@code b}
     */
    public static int multiply(int a, int b) {
        return a * b;
    }

    /**
     * Делит одно число на другое.
     *
     * @param a делимое
     * @param b делитель
     * @return результат деления {@code a} на {@code b}
     * @throws ArithmeticException если {@code b} равно {@code 0}
     */
    public static double divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Деление на ноль запрещено.");
        }
        return (double) a / b;
    }

    /**
     * Возвращает факториал заданного числа.
     *
     * <p>Пример:
     * <pre>
     *     factorial(5) возвращает 120
     * </pre>
     *
     * @param n число, для которого вычисляется факториал
     * @return факториал числа {@code n}
     * @throws IllegalArgumentException если {@code n} отрицательное
     * @see #multiply(int, int)
     */
    public static int factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Факториал отрицательного числа не определён.");
        }
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result = multiply(result, i);
        }
        return result;
    }
}