package calculator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ExpressionParser {

    enum TokenType {
        NUMBER, OPERATOR, LPAREN, RPAREN, UNARY_MINUS, PERCENT
    }

    static class Token {
        final TokenType type;
        final double value;
        final char operator;

        Token(TokenType type) {
            this.type = type;
            this.value = 0;
            this.operator = 0;
        }

        Token(double value) {
            this.type = TokenType.NUMBER;
            this.value = value;
            this.operator = 0;
        }

        Token(TokenType type, char operator) {
            this.type = type;
            this.value = 0;
            this.operator = operator;
        }
    }

    public double evaluate(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return 0;
        }
        List<Token> tokens = tokenize(expr);
        List<Token> postfix = shuntingYard(tokens);
        return evaluateRPN(postfix);
    }

    private List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = expr.length();

        while (i < len) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    num.append(expr.charAt(i));
                    i++;
                }
                tokens.add(new Token(Double.parseDouble(num.toString())));
                continue;
            }

            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN));
                i++;
                continue;
            }

            if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN));
                i++;
                continue;
            }

            if (c == '%') {
                tokens.add(new Token(TokenType.PERCENT));
                i++;
                continue;
            }

            if (c == '+' || c == '-' || c == '*' || c == '/') {
                if (c == '-') {
                    boolean isUnary = tokens.isEmpty()
                            || tokens.get(tokens.size() - 1).type == TokenType.LPAREN
                            || tokens.get(tokens.size() - 1).type == TokenType.OPERATOR
                            || tokens.get(tokens.size() - 1).type == TokenType.UNARY_MINUS;
                    if (isUnary) {
                        tokens.add(new Token(TokenType.UNARY_MINUS));
                        i++;
                        continue;
                    }
                }
                tokens.add(new Token(TokenType.OPERATOR, c));
                i++;
                continue;
            }

            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        return tokens;
    }

    private int precedence(char op) {
        switch (op) {
            case '+': case '-': return 1;
            case '*': case '/': return 2;
            default: return 0;
        }
    }

    private List<Token> shuntingYard(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();

        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER:
                    output.add(token);
                    break;

                case UNARY_MINUS:
                case PERCENT:
                    stack.push(token);
                    break;

                case OPERATOR:
                    while (!stack.isEmpty()) {
                        Token top = stack.peek();
                        if (top.type == TokenType.OPERATOR
                                && precedence(top.operator) >= precedence(token.operator)) {
                            output.add(stack.pop());
                        } else {
                            break;
                        }
                    }
                    stack.push(token);
                    break;

                case LPAREN:
                    stack.push(token);
                    break;

                case RPAREN:
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LPAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Mismatched parentheses");
                    }
                    stack.pop();
                    break;
            }
        }

        while (!stack.isEmpty()) {
            Token token = stack.pop();
            if (token.type == TokenType.LPAREN || token.type == TokenType.RPAREN) {
                throw new IllegalArgumentException("Mismatched parentheses");
            }
            output.add(token);
        }

        return output;
    }

    private double evaluateRPN(List<Token> postfix) {
        Deque<Double> stack = new ArrayDeque<>();

        for (Token token : postfix) {
            switch (token.type) {
                case NUMBER:
                    stack.push(token.value);
                    break;

                case UNARY_MINUS:
                    if (stack.isEmpty()) throw new ArithmeticException("Invalid expression");
                    stack.push(-stack.pop());
                    break;

                case PERCENT:
                    if (stack.isEmpty()) throw new ArithmeticException("Invalid expression");
                    stack.push(stack.pop() / 100.0);
                    break;

                case OPERATOR:
                    if (stack.size() < 2) throw new ArithmeticException("Invalid expression");
                    double b = stack.pop();
                    double a = stack.pop();
                    switch (token.operator) {
                        case '+' -> stack.push(a + b);
                        case '-' -> stack.push(a - b);
                        case '*' -> stack.push(a * b);
                        case '/' -> {
                            if (b == 0) throw new ArithmeticException("Division by zero");
                            stack.push(a / b);
                        }
                        default -> throw new IllegalArgumentException("Unknown operator: " + token.operator);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected token in RPN: " + token.type);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return stack.pop();
    }
}
