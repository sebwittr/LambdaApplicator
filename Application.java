import java.util.HashSet;

public class Application implements Expression {
    private Expression left;
    private Expression right;
    public Application(Expression left, Expression right){
        this.left = left;
        this.right = right;
    }
    public Expression copy() {
        return new Application(left, right);
    }

    public boolean equals(Object other) {
        if(!(other instanceof Application)){
            return false;
        }
        return ((Application) other).getLeft().equals(left) && ((Application) other).getRight().equals(right);
    }

    @Override
    public Expression alphaConvert(String from, String to, boolean capture) {
        Expression l = left.alphaConvert(from, to, capture);
        Expression r = right.alphaConvert(from, to, capture);
        return new Application(l, r);
    }

    @Override
    public HashSet<String> allVariables() {
        HashSet<String> vars = new HashSet<>();
        HashSet<String> le = left.allVariables();
        HashSet<String> ri = right.allVariables();
        for (String x: le) {
            vars.add(x);
        }
        for(String r: ri) {
            vars.add(r);
        }
        return vars;
    }

    @Override
    public HashSet<String> boundVariables() {
        HashSet<String> vars = left.boundVariables();
        HashSet<String> ri = right.boundVariables();
        for(String r: ri){
            vars.add(r);
        }
        return vars;
    }

    public Expression stabilize(Expression expres){
        if(expres instanceof Application){
            Expression newExpr = expres.eval();
            while(!(newExpr.equals(expres))){
                expres = newExpr;
                newExpr = expres.eval();
            }
        }
        return expres;
    }

    @Override
    public Expression eval() {
        Expression newleft = stabilize(left);
        Expression newright = right;
        if (right instanceof Application)
            newright = stabilize(right);
        if(left instanceof Function){
            if(newright instanceof Name){
                if(newleft.boundVariables().contains(newright.toString())){
                    int i = 1;
                    while(this.boundVariables().contains(newright+""+i)){
                        i++;
                    }
                    newleft = newleft.alphaConvert(newright.toString(), newright+""+i, false);
                }
            }
            return ((Function) newleft).getRight().replace(((Function) newleft).getLeft().toString(),newright);
        }
        else{
            return new Application(newleft, newright);
        }

    }
    public Expression getLeft(){
        return left;
    }
    public Expression getRight(){
        return right;
    }
    @Override
    public Expression replace(String from, Expression to) {
        return new Application (left.replace(from, to), right.replace(from, to));
    }
    public String toString(){
        return "(" + left + " " + right + ")";
    }
}
