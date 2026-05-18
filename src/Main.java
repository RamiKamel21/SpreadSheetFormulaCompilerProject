import java.util.*;



public class Main {
    private static final Map<String, Double> mockSheet = new HashMap<>();

    static {

        mockSheet.put("A1", 10.0);
        mockSheet.put("A2", 20.0);
        mockSheet.put("B1", 5.0);
        mockSheet.put("B2", 2.0);
        mockSheet.put("C1", 100.0);
    }

    public static void main(String[] args) {
        System.out.println("=== Spreadsheet Formula Interpreter (OOP) ===");
        System.out.println("Available Data: A1=10, A2=20, B1=5, B2=2, C1=100");

        java.util.Scanner sc = new java.util.Scanner(System.in);
        while (true) {
            System.out.print("\nEnter Formula (e.g., =SUM(A1, A2) * B1) or type 'exit' to quit: ");
            String input = sc.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) break;
            if (input.isEmpty()) continue;

            processFormula(input);
        }
        sc.close();

    }

    private static void processFormula(String input) {
        try {

            Scanner scanner = new Scanner(input);
            List<Token> tokens = scanner.scanTokens();
            System.out.println("[Tokens]: " + tokens);


            Parser parser = new Parser(tokens);
            Node ast = parser.parse();


            System.out.println("[AST Structure]:\n" + ast.print("  "));


            double result = ast.evaluate(mockSheet);
            System.out.println("[Final Result]: " + result);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}