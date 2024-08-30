class ExpressionProcessor
{
  public Map<Character, Integer> variables = new HashMap<>();

  public int calculate(String expression)
  {
    // todo
    Lexer expressionLexicons = new Lexer(expression);
    List<Token> expressionTokens = expressionLexicons.tokens;
    Parsable parsableExpression;
    
    try {
        parsableExpression = new IntegerElement(Integer.parseInt(expressionTokens.get(0).text));
    
        for(Token token : expressionTokens.subList(1, expressionTokens.size())) {
            if(token.type == Token.Type.INTEGER) {
                BinaryOperator parsableExpressionBO = (BinaryOperator) parsableExpression;
                parsableExpressionBO.setRight(new IntegerElement(Integer.parseInt(token.text)));
            } else if(token.type == Token.Type.VARIABLE) {
                if (token.text.length() > 1) {
                    return invalidExpressionOutput(new Exception(String.format("Malform variable (%s)!", token.text)));
                }
                
                Integer varToInt = variables.get(token.text.charAt(0));
                if (varToInt == null) {
                    return invalidExpressionOutput(new Exception(String.format("Variable (%s) not found!", token.text)));
                }
                
                BinaryOperator parsableExpressionBO = (BinaryOperator) parsableExpression;
                parsableExpressionBO.setRight(new IntegerElement(varToInt));
            } else {
                switch(token.type) {
                    case ADD:
                        parsableExpression = new BinaryOperator(parsableExpression, null, BinaryOperator.Type.ADD);
                        break;
                    case SUBTRACT:
                        parsableExpression = new BinaryOperator(parsableExpression, null, BinaryOperator.Type.SUBTRACT);
                        break;
                }
            }
        }
    } catch(Exception e) {
        return invalidExpressionOutput(e);
    }
    
    return parsableExpression.eval();
  }
  
  private int invalidExpressionOutput(Exception e) {
      System.out.println(e);
      return 0;
  }
}

class Token {
    Type type;
    String text;
    
    public enum Type {
        INTEGER,
        ADD,
        SUBTRACT,
        VARIABLE
    }
    
    public Token(Type type, String text) {
        this.type = type;
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }
}

class Lexer {
    public List<Token> tokens = new ArrayList<Token>();
    
    public Lexer(String expression) {
        for(int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);
            switch(current) {
                case '+':
                    tokens.add(new Token(Token.Type.ADD, Character.toString(current)));
                    break;
                case '-':
                    tokens.add(new Token(Token.Type.SUBTRACT, Character.toString(current)));
                    break;
                default:
                    StringBuilder currentExpression = new StringBuilder("" + current);
                    i = combinator(expression, i, currentExpression);
                    if(Character.toString(current).matches("[0-9]")) {
                        tokens.add(new Token(Token.Type.INTEGER, currentExpression.toString()));
                    } else {
                        tokens.add(new Token(Token.Type.VARIABLE, currentExpression.toString()));
                    }
                    break;
            }
        }
    }
    
    public int combinator(String expression, int i, StringBuilder subExpression) {
        char current = expression.charAt(i);
        String regexPattern = Character.toString(current).matches("[0-9]") ? "[0-9]" : "[a-zA-Z]";
        
        for(int j = i + 1; j < expression.length(); j++) {
            char subChar = expression.charAt(j);
            if(Character.toString(subChar).matches(regexPattern)) {
                subExpression.append(subChar);
            } else {
                return j - 1;
            }
        }
        
        return expression.length() - 1;
    }
}

interface Parsable {
    int eval();
}

class IntegerElement implements Parsable {
    private int value;
    
    public IntegerElement(int value) {
        this.value = value;
    }
    
    @Override
    public int eval() {
        return value;
    }
}

class BinaryOperator implements Parsable {
    private Parsable left, right;
    public Type type;
    
    public enum Type {
        ADD,
        SUBTRACT
    }
    
    public BinaryOperator(Parsable left, Parsable right, Type type) {
        this.setLeft(left);
        this.setRight(right);
        this.type = type;
    }
    
    public void setLeft(Parsable left) throws IllegalArgumentException {
        if(left != null) {
            if(left.getClass().getSimpleName() == "BinaryOperator") {
                BinaryOperator leftBO = (BinaryOperator) left;
                if(leftBO.right == null) {
                    throw new IllegalArgumentException("Invalid expression, contains incomplete binary operator!");
                }
            }
        }
        this.left = left;
    }
    
    public void setRight(Parsable right) throws IllegalArgumentException {
        if(right != null) {
            if(right.getClass().getSimpleName() == "BinaryOperator") {
                BinaryOperator rightBO = (BinaryOperator) right;
                if(rightBO.left == null) {
                    throw new IllegalArgumentException("Invalid expression, contains incomplete binary operator!");
                }
            }
        }
        this.right = right;
    }
    
    @Override
    public int eval() {
        int result;
        
        switch(type) {
            case ADD:
                result = left.eval() + right.eval();
                break;
            case SUBTRACT:
                result = left.eval() - right.eval();
                break;
            default:
                result = 0;
                break;
        }
        return result;
    }
}