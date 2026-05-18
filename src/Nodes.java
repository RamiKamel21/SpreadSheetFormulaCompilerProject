import java.util.*;
import java.util.stream.Collectors;


abstract class Node {
    public abstract String print(String indent);
    public abstract double evaluate(Map<String, Double> dataContext) throws Exception;
    public abstract Map<String, Object> toJsonMap();
}

// Mariam Saeed Id:20230540
class FormulaNode extends Node {
    private final Node expression;

    public FormulaNode(Node expression) {
        this.expression = expression;
    }

    @Override
    public String print(String indent) {
        return indent + "Formula\n" + expression.print(indent + "  ");
    }

    @Override
    public double evaluate(Map<String, Double> ctx) throws Exception {
        return expression.evaluate(ctx);
    }
    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Formula");
        map.put("expression", expression.toJsonMap());
        return map;
    }
}
// Salma Kamal ID:20230256
class BinaryOpNode extends Node {
    private final String op;
    private final Node left, right;

    public BinaryOpNode(String op, Node left, Node right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String print(String indent) {
        return indent + "BinaryOp(" + op + ")\n" + left.print(indent + "  ") + right.print(indent + "  ");
    }

    @Override
    public double evaluate(Map<String, Double> ctx) throws Exception {
        double l = left.evaluate(ctx);
        double r = right.evaluate(ctx);

        return switch (op) {
            case "+" -> l + r;
            case "-" -> l - r;
            case "*" -> l * r;
            case "/" -> {
                if (r == 0) throw new Exception("Division by zero");
                yield l / r;
            }
            case "==" -> (l == r) ? 1.0 : 0.0;
            case ">"  -> (l > r) ? 1.0 : 0.0;
            case "<"  -> (l < r) ? 1.0 : 0.0;
            case ">=" -> (l >= r) ? 1.0 : 0.0;
            case "<=" -> (l <= r) ? 1.0 : 0.0;
            default -> throw new Exception("Unknown operator: " + op);
        };
    }
    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "BinaryOp");
        map.put("operator", op);
        map.put("left", left.toJsonMap());
        map.put("right", right.toJsonMap());
        return map;
    }
}

// Salma Kamal ID:20230256
class UnaryOpNode extends Node {
    private final String op;
    private final Node right;

    public UnaryOpNode(String op, Node right) {
        this.op = op;
        this.right = right;
    }

    @Override
    public String print(String indent) {
        return indent + "UnaryOp(" + op + ")\n" + right.print(indent + "  ");
    }

    @Override
    public double evaluate(Map<String, Double> ctx) throws Exception {
        if (op.equals("-")) return -right.evaluate(ctx);
        return right.evaluate(ctx);
    }
    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "UnaryOp");
        map.put("operator", op);
        map.put("operand", right.toJsonMap());
        return map;
    }
}


class LiteralNode extends Node {
    private final double value;

    public LiteralNode(String val) {
        this.value = Double.parseDouble(val);
    }

    @Override
    public String print(String indent) {
        return indent + "Literal(" + value + ")\n";
    }

    @Override
    public double evaluate(Map<String, Double> ctx) {
        return value;
    }
    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Literal");
        map.put("value", value);
        return map;
    }
}

//Toka Adbelaziz Id:20230142
class CellNode extends Node {
    private final String ref;

    public CellNode(String ref) {
        this.ref = ref;
    }

    @Override
    public String print(String indent) {
        return indent + "Cell(" + ref + ")\n";
    }

    @Override
    public double evaluate(Map<String, Double> ctx) throws Exception {
        if (!ctx.containsKey(ref)) {
            throw new Exception("Reference Error: Cell " + ref + " is undefined.");
        }
        return ctx.get(ref);
    }
    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Cell");
        map.put("reference", ref);
        return map;
    }
}

//Rami Kamel Erian Id:20230199
class FunctionNode extends Node {
    private final String name;
    private final List<Node> args;

    public FunctionNode(String name, List<Node> args) {
        this.name = name;
        this.args = args;
    }

    @Override
    public String print(String indent) {
        StringBuilder sb = new StringBuilder(indent + "Function(" + name + ")\n");
        for (Node arg : args) {
            sb.append(arg.print(indent + "  "));
        }
        return sb.toString();
    }
    @Override
    public Map<String, Object> toJsonMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Function");
        map.put("name", name.toUpperCase());
        map.put("arguments", args.stream().map(Node::toJsonMap).collect(Collectors.toList()));
        return map;
    }

    @Override
    public double evaluate(Map<String, Double> ctx) throws Exception {
        List<Double> evaluatedArgs = new ArrayList<>();
        for (Node arg : args) evaluatedArgs.add(arg.evaluate(ctx));

        return switch (name.toUpperCase()) {
            case "SUM" -> evaluatedArgs.stream().mapToDouble(Double::doubleValue).sum();
            case "AVERAGE" -> evaluatedArgs.isEmpty() ? 0 : evaluatedArgs.stream().mapToDouble(d -> d).average().orElse(0);
            case "MAX" -> evaluatedArgs.stream().mapToDouble(d -> d).max().orElse(0);
            case "MIN" -> evaluatedArgs.stream().mapToDouble(d -> d).min().orElse(0);
            case "IF" -> {
                if (evaluatedArgs.size() != 3) throw new Exception("IF requires 3 arguments: (condition, true_val, false_val)");
                yield (evaluatedArgs.get(0) != 0) ? evaluatedArgs.get(1) : evaluatedArgs.get(2);
            }
            case "ABS" -> {
                if (evaluatedArgs.size() != 1) throw new Exception("ABS requires 1 argument");
                yield Math.abs(evaluatedArgs.get(0));
            }
            case "COUNT" -> (double) evaluatedArgs.size();

            case "PRODUCT" -> evaluatedArgs.isEmpty() ? 0 : evaluatedArgs.stream().reduce(1.0, (a, b) -> a * b);

            case "ROUND" -> {
                if (evaluatedArgs.size() < 1 || evaluatedArgs.size() > 2)
                    throw new Exception("ROUND requires 1 or 2 arguments: (value, [digits])");
                double value = evaluatedArgs.get(0);
                int places = (evaluatedArgs.size() == 2) ? evaluatedArgs.get(1).intValue() : 0;
                double factor = Math.pow(10, places);
                yield Math.round(value * factor) / factor;
            }

            case "SQRT" -> {
                if (evaluatedArgs.size() != 1) throw new Exception("SQRT requires 1 argument");
                if (evaluatedArgs.get(0) < 0) throw new Exception("SQRT of a negative number is not allowed");
                yield Math.sqrt(evaluatedArgs.get(0));
            }

            case "POWER" -> {
                if (evaluatedArgs.size() != 2) throw new Exception("POWER requires 2 arguments: (base, exponent)");
                yield Math.pow(evaluatedArgs.get(0), evaluatedArgs.get(1));
            }

            case "MOD" -> {
                if (evaluatedArgs.size() != 2) throw new Exception("MOD requires 2 arguments: (number, divisor)");
                yield evaluatedArgs.get(0) % evaluatedArgs.get(1);
            }

            case "AND" -> {
                if (evaluatedArgs.isEmpty()) yield 0.0;
                yield evaluatedArgs.stream().allMatch(d -> d != 0) ? 1.0 : 0.0;
            }

            case "OR" -> {
                if (evaluatedArgs.isEmpty()) yield 0.0;
                yield evaluatedArgs.stream().anyMatch(d -> d != 0) ? 1.0 : 0.0;
            }

            case "NOT" -> {
                if (evaluatedArgs.size() != 1) throw new Exception("NOT requires 1 argument");
                yield (evaluatedArgs.get(0) == 0) ? 1.0 : 0.0;
            }
            default -> throw new Exception("Function '" + name + "' is not implemented.");
        };
    }
}
