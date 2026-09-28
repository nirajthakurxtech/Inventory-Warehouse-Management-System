package inventory.util;

import java.util.Scanner;

/** Validated console input. */
public class InputHelper {

    private final Scanner scanner = new Scanner(System.in);

    public String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("  Input cannot be empty.");
        }
    }

    /** Returns the trimmed line; may be empty (used for "press Enter to keep"). */
    public String readOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt, int min) {
        while (true) {
            String line = readNonEmpty(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min) {
                    return value;
                }
                System.out.println("  Value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a whole number.");
            }
        }
    }

    public double readDouble(String prompt, double min) {
        while (true) {
            String line = readNonEmpty(prompt);
            try {
                double value = Double.parseDouble(line);
                if (value >= min) {
                    return value;
                }
                System.out.println("  Value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }
}
