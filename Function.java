import java.util.HashSet;

public class Function implements Expression {
    private Expression left;
    private Expression right;

    public Function(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public Expression copy() {
        return null;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public boolean equals(Object other) {
        if (other instanceof Function) {
            Name othName = (Name) ((Function) other).getLeft();
            Expression othExpression = ((Function) other).getRight();
            return right.equals(othExpression.alphaConvert(othName.toString(), left.toString(), true));
        }
        return false;
    }

    @Override
    public Expression alphaConvert(String from, String to, boolean capture) {
        Name var = (Name) left.alphaConvert(from, to, capture);
        Expression r = right.alphaConvert(from, to, capture);
        if (left.toString().equals(from)) {
            var = (Name) left.alphaConvert(from, to, true);
            r = right.alphaConvert(from, to, true);
        }
        return new Function(var, r);
    }

    public static void main(String[] args) {
        System.out.println(new Function(new Name("f"), new Application(new Name("f"), new Name("x"))).alphaConvert("f", "m", false));
    }

    @Override
    public HashSet<String> allVariables() {
        HashSet<String> allvar = new HashSet<>();
        allvar.add(left.toString());
        HashSet<String> r = right.allVariables();
        for (String x : r) {
            allvar.add(x);
        }
        return allvar;
    }

    @Override
    public HashSet<String> boundVariables() {
        HashSet<String> bound = new HashSet<>();
        bound.add(left.toString());
        HashSet<String> r = right.boundVariables();
        for (String x : r) {
            bound.add(x);
        }
        return bound;
    }

    @Override
    public Expression eval() {
        return new Function(left, right.eval());
    }

    @Override
    public Expression replace(String from, Expression to) {
        return new Function(left, right.replace(from, to));
    }

    public String toString() {
        return "(λ" + left + "." + right + ")";
    }
}