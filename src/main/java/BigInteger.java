import java.util.ArrayList;
import java.util.List;

public class BigInteger {
    private final List<Integer> numbers = new ArrayList<>();
    private boolean isNegative;
    private int baseDigits = 2;
    private Integer largestNumber = 99;


    public BigInteger(String bigInteger) {
        this.isNegative = bigInteger.charAt(0) == '-' ? true : false;
        if (isNegative) {
            bigInteger = bigInteger.substring(1);
        }

        for (int i = bigInteger.length(); i > 0; i -= baseDigits) {
            int end = i;
            int start = Integer.max(i - baseDigits, 0);
            final String number = bigInteger.substring(start, end);
            numbers.add(0, Integer.parseInt(number));
        }
    }

    public static void main(String[] args) {
        final BigInteger bigInteger = new BigInteger("0");
        bigInteger.decrement();
        System.out.println(bigInteger.toString());
    }

    public void increment() {
        if (isNegative)
            decrementRecursive(numbers.size() - 1);
        else
            incrementRecursive(numbers.size() - 1);

        if (numbers.size() == 1 && numbers.get(0) == 0) {
            isNegative = false;
        }
    }

    public void decrement() {
        if (numbers.size() == 1 && numbers.get(0).equals(0)) {
            isNegative = true;
            numbers.set(0, 1);
            return;
        }
        if (isNegative)
            incrementRecursive(numbers.size() - 1);
        else
            decrementRecursive(numbers.size() - 1);

    }

    @Override
    public String toString() {
        String prefix = isNegative ? "-" : "+";
        return prefix+ numbers.toString();
    }

    private void decrementRecursive(int index) {
        if (index == 0 && numbers.get(index) == 1 && numbers.size() > 1) {
            numbers.remove(0);
            return;
        }
        Integer lastDigit = numbers.get(index);
        if (lastDigit != 0) {
            numbers.set(index, lastDigit - 1);
            return;
        }
        numbers.set(index, largestNumber);
        decrementRecursive(index - 1);
    }

    private void incrementRecursive(int index) {
        if (index < 0) {
            numbers.add(0, 1);
            return;
        }
        final Integer lastDigit = numbers.get(index);
        if (lastDigit + 1 <= largestNumber) {
            numbers.set(index, lastDigit + 1);
            return;
        }
        numbers.set(index, 0);
        incrementRecursive(index - 1);
    }
}
