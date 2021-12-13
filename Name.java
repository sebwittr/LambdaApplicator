import java.util.HashSet;

public class Name implements Expression{
    private String value;


    public Name(String val){
        value = val;
    }
    public Expression copy() {
        return new Name(value);
    }


    public boolean equals(Object other) {
        if(other instanceof Name){
            Name o = (Name) other;
            return value.equals(o.value);
        }
        return false;
    }

    @Override
    public Expression alphaConvert(String from, String to, boolean capture) {
        if (capture && from.equals(value)){
            return new Name(to);
        }
        return copy();
    }

    @Override
    public HashSet<String> allVariables() {
        HashSet<String> var = new HashSet<>();
        var.add(value);
        return var;
    }

    @Override
    public HashSet<String> boundVariables() {
        return new HashSet<>();
    }

    @Override
    public Expression eval() {
        return this;
    }

    @Override
    public Expression replace(String from, Expression to) {
        if (this.value.equals(from))
            return to;
        return this;
    }

    public String toString(){
        return value;
    }
}
