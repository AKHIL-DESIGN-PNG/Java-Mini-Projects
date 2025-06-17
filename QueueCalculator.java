import java.util.Scanner;
import java.text.DecimalFormat;

public class QueueCalculator {
    private static final DecimalFormat df = new DecimalFormat("#.##########");

    /**
     * An ExpressionQueue implemented as a fixed-size circular queue (circular array).
     * It tracks its size, capacity, and the number of times the array has "rotated".
     */
    private static class ExpressionQueue {
        private final String[] queue;
        private final int capacity;
        private int front;
        private int rear;
        private int size;
        private int rotationCount;

        public ExpressionQueue(int capacity) {
            this.capacity = capacity;
            this.queue = new String[capacity];
            this.front = 0;
            this.rear = -1;
            this.size = 0;
            this.rotationCount = 0;
        }

        /** Enqueues an item. If full, dequeues the oldest item first. */
        void add(String data) {
            if (size == capacity) {
                System.out.println("(Queue is full, oldest element '" + queue[front] + "' was dropped.)");
                front = (front + 1) % capacity; // Drop the oldest element
                size--;
            }
            int prevRear = rear;
            rear = (rear + 1) % capacity;

            // This is the logic from the provided CircularQueue to count rotations
            if (prevRear == capacity - 1 && rear == 0) {
                rotationCount++;
            }

            queue[rear] = data;
            size++;
        }

        /** Adds an item to the front. If full, drops the last item. */
        void addFirst(String data) {
            if (size == capacity) {
                System.out.println("(Queue is full, last element '" + queue[rear] + "' was dropped.)");
                rear = (rear - 1 + capacity) % capacity; // Drop the last element
                size--;
            }
            if (isEmpty()) { // Special case for first element
                front = 0;
                rear = 0;
            } else {
                front = (front - 1 + capacity) % capacity;
            }
            queue[front] = data;
            size++;
        }
        
        /** Gets an item by its logical index from the front. */
        String get(int index) {
            if (index < 0 || index >= size) return null;
            int actualIndex = (front + index) % capacity;
            return queue[actualIndex];
        }
        
        /** Gets the last item in the queue. */
        String getLast() {
            if (isEmpty()) return null;
            return queue[rear];
        }

        int size() { return size; }
        int getCapacity() { return capacity; }
        int getRotationCount() { return rotationCount; }
        boolean isEmpty() { return size == 0; }

        void clear() {
            front = 0;
            rear = -1;
            size = 0;
            rotationCount = 0; // Reset for new calculation
        }
        
        void addAll(ExpressionQueue other) {
            for (int i = 0; i < other.size(); i++) {
                this.add(other.get(i));
            }
        }

        @Override
        public String toString() {
            if (isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size; i++) {
                int index = (front + i) % capacity;
                sb.append(queue[index]);
            }
            return sb.toString();
        }
    }

    private static class ParenthesisResult {
        double result;
        ExpressionQueue expression;
        ParenthesisResult(double result, ExpressionQueue expression) {
            this.result = result;
            this.expression = expression;
        }
    }

    public static void runCalculator(Scanner sc) {
        System.out.println("\n--- Running Calculator with Circular Queue (Capacity: 30) ---");
        double result = getFirstDigit(sc);
        // Initialize the main expression queue with a capacity of 30
        ExpressionQueue expression = new ExpressionQueue(30);
        expression.add(formatNumber(result));
        printStatus(expression, result);

        mainLoop:
        while (true) {
            printMainMenu();
            int choice = getIntInput(sc, 1, 11);
            boolean printStatus = true;

            switch (choice) {
                case 1: double addNum = getNextDigit(sc); result += addNum; expression.add(" + "); expression.add(formatNumber(addNum)); break;
                case 2: double subNum = getNextDigit(sc); result -= subNum; expression.add(" - "); expression.add(formatNumber(subNum)); break;
                case 3: double mulNum = getNextDigit(sc); result *= mulNum; expression.add(" * "); expression.add(formatNumber(mulNum)); break;
                case 4: double divNum = getNextDigit(sc); if (divNum == 0) { System.out.println("Cannot divide by zero."); } else { result /= divNum; expression.add(" / "); expression.add(formatNumber(divNum)); } break;
                case 5:
                    System.out.println("---------------------------------");
                    System.out.println("Final Expression: " + expression.toString() + " = " + formatNumber(result));
                    System.out.println("Final Queue Status: Size=" + expression.size() + "/" + expression.getCapacity() + ", Rotations=" + expression.getRotationCount());
                    System.out.println("---------------------------------");
                    System.out.println("\nStarting new calculation...");
                    result = getFirstDigit(sc);
                    expression.clear();
                    expression.add(formatNumber(result));
                    break;
                case 6: System.out.println("...Returning to main menu..."); sc.nextLine(); return;
                case 7: int opChoice = chooseParenthesisOperator(sc); if (opChoice == 9) { printStatus = false; break; } ParenthesisResult pr = handleParenthesis(sc); if (pr.expression.isEmpty()) { printStatus = false; break; } switch (opChoice) { case 1: result+=pr.result; expression.add(" + ("); expression.addAll(pr.expression); expression.add(")"); break; case 2: result-=pr.result; expression.add(" - ("); expression.addAll(pr.expression); expression.add(")"); break; case 3: result*=pr.result; expression.add(" * ("); expression.addAll(pr.expression); expression.add(")"); break; case 4: if (pr.result==0){System.out.println("No div by zero.");}else{result/=pr.result; expression.add(" / ("); expression.addAll(pr.expression); expression.add(")");} break; case 5: result=Math.pow(result, pr.result); expression.add(" ^ ("); expression.addAll(pr.expression); expression.add(")"); break; case 6: if (pr.result==0){System.out.println("No mod by zero.");}else{result%=pr.result; expression.add(" % ("); expression.addAll(pr.expression); expression.add(")");} break; case 7: if (pr.result<0){System.out.println("Sqrt of negative.");}else{result=Math.sqrt(pr.result); expression.add(" √("); expression.addAll(pr.expression); expression.add(")");} break; case 8: if(pr.result<0||pr.result!=(int)pr.result||pr.result>20){System.out.println("Factorial undefined.");}else{result=factorial((int)pr.result); expression.add(" ("); expression.addAll(pr.expression); expression.add(")!");} break; } break;
                case 8: if (result < 0) { System.out.println("Sqrt of negative."); } else { result = Math.sqrt(result); expression.addFirst("√("); expression.add(")"); } break;
                case 9: System.out.print("Enter exponent: "); double exp = getNextDigit(sc); result = Math.pow(result, exp); expression.add(" ^ " + formatNumber(exp)); break;
                case 10: double mod = getNextDigit(sc); if (mod == 0) { System.out.println("Cannot mod by zero."); } else { result %= mod; expression.add(" % "); expression.add(formatNumber(mod)); } break;
                case 11:
                    if (expression.size() >= 2) { String lastNumStr = expression.getLast(); try { double lastValue = Double.parseDouble(lastNumStr); if (lastValue < 0 || lastValue != (int) lastValue || lastValue > 20) { System.out.println("Factorial undefined."); break; } long factResult = factorial((int) lastValue); String operator = expression.get(expression.size() - 2).trim(); switch (operator) { case "+": result = (result - lastValue) + factResult; break; case "-": result = (result + lastValue) - factResult; break; case "*": result = (result / lastValue) * factResult; break; case "/": result = (result * lastValue) / factResult; break; default: System.out.println("Cannot apply ! after " + operator); continue mainLoop; } expression.add("!"); } catch (NumberFormatException e) { System.out.println("Cannot apply factorial here."); } } else { if (result < 0 || result != (int) result || result > 20) { System.out.println("Factorial undefined."); } else { result = factorial((int) result); expression.add("!"); } } break;
            }
            if(printStatus) {
                printStatus(expression, result);
            }
        }
    }
    
    private static void printStatus(ExpressionQueue expression, double result) {
        System.out.println("=> Expression: " + expression.toString());
        System.out.println("=> Result: " + formatNumber(result));
        System.out.println("=> Queue Status: Size=" + expression.size() + "/" + expression.getCapacity() + ", Rotations=" + expression.getRotationCount());
    }

    private static void printMainMenu() { System.out.println("\n--- Calculator Menu (Queue) ---"); System.out.println("1.Add(+) 2.Sub(-) 3.Mul(*) 4.Div(/) 5.Result(=)"); System.out.println("6.Return to Main Menu 7.Parenthesis() 8.Sqrt(√) 9.Power(^) 10.Mod(%) 11.Factorial(!)"); System.out.print("Choose an operation (1-11): "); }
    private static int chooseParenthesisOperator(Scanner sc) { System.out.println("Operator for parenthesis: 1.+ 2.- 3.* 4./ 5.^ 6.% 7.√ 8.! 9.Cancel"); return getIntInput(sc, 1, 9); }
    private static ParenthesisResult handleParenthesis(Scanner sc) {
        System.out.println("\n--() Entering Parenthesis Mode (Capacity: 20) ()--");
        double result = getFirstDigit(sc);
        // Initialize parenthesis queue with a capacity of 20
        ExpressionQueue expr = new ExpressionQueue(20);
        expr.add(formatNumber(result));
        printStatus(expr, result);

        while (true) {
            System.out.println("\n--()Menu--\n1.+ 2.- 3.* 4./ 5.= (Finish) 6.Cancel 7.√ 8.^ 9.% 10.!");
            int choice = getIntInput(sc, 1, 10);
            if (choice == 5) { System.out.println("--() Exiting Parenthesis Mode ()--\n"); return new ParenthesisResult(result, expr); }
            if (choice == 6) { System.out.println("--() Canceling Parenthesis Mode ()--\n"); return new ParenthesisResult(0, new ExpressionQueue(0)); }
            double nextDigit;
            switch (choice) { case 1: nextDigit=getNextDigit(sc); result+=nextDigit; expr.add(" + "); expr.add(formatNumber(nextDigit)); break; case 2: nextDigit=getNextDigit(sc); result-=nextDigit; expr.add(" - "); expr.add(formatNumber(nextDigit)); break; case 3: nextDigit=getNextDigit(sc); result*=nextDigit; expr.add(" * "); expr.add(formatNumber(nextDigit)); break; case 4: nextDigit=getNextDigit(sc); if(nextDigit==0){System.out.println("No div by zero.");}else{result/=nextDigit; expr.add(" / "); expr.add(formatNumber(nextDigit));} break; case 7: if(result<0){System.out.println("Sqrt of negative.");}else{result=Math.sqrt(result); expr.addFirst("√("); expr.add(")");} break; case 8: System.out.print("Exponent: "); double exp=getNextDigit(sc); result=Math.pow(result,exp); expr.add(" ^ "+formatNumber(exp)); break; case 9: nextDigit=getNextDigit(sc); if(nextDigit==0){System.out.println("No mod by zero.");}else{result%=nextDigit; expr.add(" % "); expr.add(formatNumber(nextDigit));} break; case 10: if(result<0||result!=(int)result||result>20){System.out.println("Invalid for factorial.");}else{result=factorial((int)result); expr.add("!");} break; }
            printStatus(expr, result);
        }
    }
    private static double getFirstDigit(Scanner sc) { System.out.print("Enter the first digit: "); while(!sc.hasNextDouble()){ System.out.print("Enter a number: "); sc.next(); } double val = sc.nextDouble(); sc.nextLine(); return val; }
    private static double getNextDigit(Scanner sc) { System.out.print("Enter the next digit: "); while(!sc.hasNextDouble()){ System.out.print("Enter a number: "); sc.next(); } double val = sc.nextDouble(); sc.nextLine(); return val; }
    private static int getIntInput(Scanner sc, int min, int max) { while (true) { System.out.print("Choose option ("+min+"-"+max+"): "); String line = sc.nextLine(); try { int val = Integer.parseInt(line.trim()); if (val >= min && val <= max) return val; } catch (NumberFormatException e) { /* Do nothing */ } } }
    private static long factorial(int n) { if (n < 0) return 0; if (n == 0 || n == 1) return 1; long res = 1; for (int i = 2; i <= n; i++) res *= i; return res; }
    private static String formatNumber(double num) { if (num == (long) num) { return String.valueOf((long) num); } else { return df.format(num); } }
    
    // The main method from the original CircularQueue class is not needed here,
    // as its logic has been integrated above.
    // public static void main(String[] args) {}
}