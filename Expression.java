import java.util.HashSet;

public interface Expression {
    public Expression copy();
    public Expression alphaConvert(String from, String to, boolean capture);
    public HashSet<String> allVariables();
    public HashSet<String> boundVariables();
    public Expression eval();
    public Expression replace(String from, Expression to);
}
