
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
/*
0 = \f.\x.x
succ = \n.\f.\x.f (n f x)
1 = run succ 0
+ = λm.λn.λf.λx.(m f) ((n f) x)
* = λn.λm.λf.λx.n (m f) x
2 = run succ 1
3 = run + 2 1
4 = run * 2 2
5 = (λf.(λx.(f (f (f (f (f x)))))))
7 = run succ (succ 5)
pred = λn.λf.λx.n (λg.λh.h (g f)) (λu.x) (λu.u)
6 = run pred 7
- = λm.λn.(n pred) m
10 = run succ (+ 3 6)
9 = run pred 10
not = λp.p false true
even? = λn.n not true
odd? = \x.not (even? x)
run even? 0
run even? 5


* */

public class main {
    public static HashMap<String, Expression> variables = new HashMap<>();

    public static ArrayList<String> splitTopLevelArgs(String e){
        ArrayList<String> tokens = new ArrayList<>();
        String str = "";
        int numParen = 0;
        boolean lambda = false;
        int lParen = 0;
        for (int i = 0; i < e.length(); i++) {
            char c = e.charAt(i);
            if(c == ' ' && numParen == 0 && !lambda){
                if (!str.equals(" ") && !str.equals("")) {
                    tokens.add(str);
                    }str = "";

            }
            else if(c == '.' && !lambda){
                lParen = numParen;
                str += ".";
                lambda = true;
                if(lParen == 0){
                    tokens.add(str);
                    str = "";
                }
                if (e.charAt(i+1) == ' ')
                    i++;
            }
            else if(c == '('){
                str += "(";
                numParen++;
            }
            else if(c == ')'){
                str += ")";
                if(numParen == lParen){
                    lambda = false;
                }
                numParen--;
            }
            else{
                str += c;
            }
        }
        if(!(str.equals(""))){
            tokens.add(str);
        }
        return tokens;
    }
    public static boolean spaceName (String inp){
        String n = "";
        for (int i = 0; i < inp.length() - 1 ; i++) {
            n += inp.charAt(i);
        }
        return isName(n) && (inp.charAt(inp.length() - 1) == ' ');
    }
    public static Expression lambdify(String inp, boolean recurse){
        if (inp.equals("")) {
            return new Name("\n");
        }
        if(recurse){
            inp = removeFirstParen(inp);
        }
        if (isName(inp)){
            ArrayList<String> t = new ArrayList<>();
            t.add(inp);
            String replaced = replaceVariables(t, false).get(0);
            if (isName(replaced))
                return new Name(replaced);
            return lambdify(replaced, false);
        }
        if(spaceName(inp)){
            return new Name(inp.replaceAll(" ", ""));
        }
        ArrayList<String> tokens = splitTopLevelArgs(inp);
        if(tokens.size() == 1){
            tokens = replaceVariables(tokens, false);
            return lambdify(tokens.get(0), true);
        }
        if(tokens.get(1).equals("=")){
            tokens = replaceVariables(tokens, true);

            if(variables.containsKey(tokens.get(0))){
                return new Name(tokens.get(0) + " is already defined");
            }
            if(tokens.get(0).equals("run")){
                return new Name("run cannot be a variable name [use run to evaluate an expression]");
            }
            tokens.remove(1);

            if(tokens.size() == 2){
                variables.put(tokens.get(0), lambdify(tokens.get(1), false));
                return new Name("Added " + lambdify(tokens.get(1), false) + " as " + tokens.get(0));
            }
            String variablename = tokens.remove(0);
            if (tokens.size() == 2 && tokens.get(0).equals("run")){
                if(isName(tokens.get(1))){
                    variables.put(variablename, new Name(tokens.get(1)));
                    return new Name("Added " + new Name(tokens.get(1)) + " as " + variablename);
                }
                if(isLambda(tokens.get(1)) && !tokens.get(1).contains("(")){
                    variables.put(variablename, lambdify(tokens.get(1), false));
                    return new Name("Added " + lambdify(tokens.get(1), false) + " as " + variablename);
                }
                else{
                    Expression e = lambdify(tokens.get(1), true);
                    variables.put(variablename, e.eval());
                    return new Name("Added " + e.eval() + " as " + variablename);
                }
            }
            if(tokens.get(0).equals("run")){
                tokens.remove(0);
                Expression newTree = buildTree(tokens, null);
                Expression curTree = newTree.eval();
                while(!(curTree.equals(newTree))){
                    newTree = curTree;
                    curTree = curTree.eval();
                }
                variables.put(variablename, curTree);
                return new Name("Added " + curTree + " as " + variablename);
            }
            Expression newTree =  buildTree(tokens, null);
            variables.put(variablename, newTree);
            return new Name("Added " + newTree + " as " + variablename);
        }
        tokens = replaceVariables(tokens, false);
        if(tokens.get(0).equals("run")){
            tokens.remove(0);
            if(tokens.size() == 1){
                return lambdify(tokens.get(0), false);
            }
            else{
                Expression tree = buildTree(tokens, null);
                Expression curTree = tree.eval();
                while(!(curTree.equals(tree))){
                    tree = curTree;
                    curTree = curTree.eval();
                }
                return curTree;
            }
        }
        return buildTree(tokens, null);
    }

    public static ArrayList<String> replaceVariables(ArrayList<String> tokens, boolean settingVar){

        ArrayList<String> ret = new ArrayList<>();
        if(settingVar){
            ret.add(tokens.remove(0));
            ret.add(tokens.remove(0));
        }

        for (int i = 0; i < tokens.size(); i++) {
            if(contain(tokens.get(i))){
                String word = "";
                String newToken = "";
                for (int j = 0; j < tokens.get(i).length(); j++) {
                    char c = tokens.get(i).charAt(j);
                    if (c == ' ' || c == ')') {
                        newToken += variables.getOrDefault(word, new Name(word));
                        newToken += c;
                        word = "";
                    } else if (c == '(') {
                        newToken += c;
                    } else {
                        word += c;
                    }
                }

                if(!word.equals("")){
                    newToken += variables.getOrDefault(word, new Name(word)).toString();
                }

                ret.add(newToken);
            }
            else{
                ret.add(tokens.get(i));
            }
        }
        return ret;
    }
    public static boolean contain(String token){
        String cur = "";
        boolean ret = false;
        for (int i = 0; i < token.length() ; i++) {
            char c = token.charAt(i);
            if(c == ' '){
                if(variables.containsKey(cur)){
                    ret = true;
                }
                cur = "";
            }
            else if(c != '(' && c != ')'){
                cur += c;
            }
        }
        if(variables.containsKey(cur)){
            ret = true;
        }
        return ret;
    }
    public static String removeFirstParen(String inp){
        String input = "";
        for (int i = 1; i < inp.length() -1; i++) {
            input += inp.charAt(i);
        }
        return input;
    }

    public static boolean isName(String inp){
        char [] specialcharacters = new char[] {' ', '(', ')' , '.', '\\', 'λ'};
        for (int i = 0; i < inp.length(); i++) {
            char c = inp.charAt(i);
            for (int j = 0; j < specialcharacters.length; j++) {
                if(c == specialcharacters[j]){
                    return false;
                }
            }
        }
        return true;
    }
    public static String getRight(String inp){
        String s = "";
        boolean add = false;
        for (int i = 0; i < inp.length() - 1; i++) {
            char c = inp.charAt(i);
            if(c == '.'){
                add = true;
            }
            else if(add){
                s += c;
            }
        }
        return s;
    }
    public static Expression buildTree(ArrayList<String> tokens, Expression left) {
        if (tokens.size() == 0) {
            return left;
        }
        if (left == null) {
            if(isLambda(tokens.get(0))){
                if(tokens.get(0).charAt(0) == '('){
                    if(tokens.get(1).charAt(0) == '('){
                        left = new Application(lambdify(tokens.remove(0), true), lambdify(tokens.remove(0), true));
                    }
                    else{
                        left = new Application(lambdify(tokens.remove(0), true), lambdify(tokens.remove(0), false));
                    }
                    return buildTree(tokens, left);

                }
                else{
                    return new Function(lambdify(findName(tokens.remove(0)), false), lambdify(tokens.remove(0), false));
                }
            }
            if(isLambda(tokens.get(1))){

                if(tokens.get(0).charAt(0) == '('){
                    Function right;
                    if(tokens.get(1).charAt(0) == '('){
                        left = new Application(lambdify(tokens.remove(0), true), lambdify(tokens.remove(0), true));
                        return buildTree(tokens, left);
                    }
                    else{
                        if(tokens.get(2).charAt(0) == '('){
                            right = new Function(lambdify(findName(tokens.remove(1)), false), lambdify(tokens.remove(1), true));
                        }
                        else{
                            right = new Function(lambdify(findName(tokens.remove(1)), false), lambdify(tokens.remove(1), false));
                        }
                        left = new Application(lambdify(tokens.remove(0), true), right);
                        return buildTree(tokens, left);
                    }
                }
                else{
                    Function right;
                    if(tokens.get(1).charAt(0) == '('){
                        left = new Application(lambdify(tokens.remove(0), false), lambdify(tokens.remove(0), true));
                        return buildTree(tokens, left);
                    }
                    else{
                        if(tokens.get(2).charAt(0) == '('){
                            right = new Function(lambdify(findName(tokens.remove(1)), false), lambdify(tokens.remove(1), true));
                        }
                        else{

                            right = new Function(lambdify(findName(tokens.remove(1)), false), lambdify(tokens.remove(1), false));
                        }
                        left = new Application(lambdify(tokens.remove(0), false), right);
                        return buildTree(tokens, left);
                    }
                }
            }
            else {
                if (tokens.get(0).charAt(0) == '(' && tokens.get(1).charAt(0) == '(') {
                    left = new Application(lambdify(tokens.remove(0), true), lambdify(tokens.remove(0), true));
                } else if (tokens.get(0).charAt(0) == '(' && tokens.get(1).charAt(0) != '(') {
                    left = new Application(lambdify(tokens.remove(0), true), lambdify(tokens.remove(0), false));
                } else if (tokens.get(0).charAt(0) != '(' && tokens.get(1).charAt(0) == '(') {
                    left = new Application(lambdify(tokens.remove(0), false), lambdify(tokens.remove(0), true));
                } else {
                    left = new Application(lambdify(tokens.remove(0), false), lambdify(tokens.remove(0), false));
                }
            }
            return buildTree(tokens, left);
        }
        else {
            if (isLambda(tokens.get(0))) {
                if (tokens.get(0).charAt(0) == '(') {
                    left = new Application(left, lambdify(tokens.remove(0), false));
                } else {
                    System.out.println(tokens.get(0));
                    left = new Application(left, new Function(lambdify(findName(tokens.remove(0)), false), lambdify(tokens.remove(0), false)));
                }
            } else {
                if (tokens.get(0).charAt(0) == '(') {
                    left = new Application(left, lambdify(tokens.remove(0), true));
                } else {
                    left = new Application(left, lambdify(tokens.remove(0), false));
                }
            }
        }
            return buildTree(tokens, left);
    }
    public static boolean isLambda(String inp){
        for (int i = 0; i < inp.length(); i++) {
            char c = inp.charAt(i);
            if(c == '\\' || c == 'λ'){
                return true;
            }
        }
        return false;
    }
    public static String findName(String word){
        String ret ="";
        boolean add = false;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if(c == '.'){
                break;
            }
            if(add){
                ret += c;
            }
            else if(c == '\\' || c == 'λ'){
                add = true;
            }
        }
        return ret;
    }
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String expression;

        while(true){
            System.out.print("> ");
            expression = input.nextLine();
            expression.replaceAll("\uFEFF", "");
            if(expression.equals("exit")){
                break;
            }
            else {
                Expression lambdified = lambdify(expression, false);
                if(!lambdified.equals(new Name("\n"))){
                    System.out.println(lambdified);
                }
            }
        }
        System.out.println("Goodbye!");
    }
}
