package parser;

import java.util.LinkedList;

import parser.token.Token;
import parser.token.Tokenizer;
import parser.tree.node.ExpressionNode;
import parser.tree.node.impl.AdditionExpressionNode;
import parser.tree.node.impl.ConstantExpressionNode;
import parser.tree.node.impl.ExponentiationExpressionNode;
import parser.tree.node.impl.FunctionExpressionNode;
import parser.tree.node.impl.MultiplicationExpressionNode;
import parser.tree.node.impl.VariableExpressionNode;

public class Parser {
	private LinkedList<Token> tokens = new LinkedList<>();
	private Token lookahead;
	private Tokenizer tokenizer;

	public Parser(Tokenizer tokenizer) {
		super();
		this.tokenizer = tokenizer;
	}

	public ExpressionNode parse(String toParse) {
		this.tokens = tokenizer.tokenize(toParse).getTokens();
		this.lookahead = this.tokens.getFirst();
		__print("begin: " + lookahead);

		// The parser will have one method for every non-terminal symbol of the
		// grammar that we designed in the last post. This means the method
		// expression() will parse the non-terminal symbol expression
		ExpressionNode expr = expression();

		if (lookahead.tokenType != Token.EPSILON)
			throw new ParserException("Unexpected symbol %s found", lookahead);

		return expr;
	}

	private void __print(String s) {
		System.out.println("Parser::" + s);
	}

	private void nextToken() {
		__print(lookahead.sequence + " CONSUMED!");

		tokens.pop();
		if (tokens.isEmpty()) {
			lookahead = new Token(Token.EPSILON, "", -1);
		} else {
			lookahead = tokens.getFirst();
		}

		__print("nextToken: " + lookahead);
	}

	private ExpressionNode expression() {
		__print("expression -> signed_term sum_op");

		// expression -> signed_term sum_op
		ExpressionNode signedTerm = signedTerm();
		return sumOp(signedTerm);
	}

	private ExpressionNode sumOp(ExpressionNode expr) {
		// si el siguiente token es de tipo TERMINAL , entonces se debe
		// desapilar un token y continuar con la secuencia

		if (lookahead.tokenType == Token.PLUSMINUS) {
			__print("sum_op -> PLUSMINUS term sum_op");
			// sum_op -> PLUSMINUS term sum_op

			AdditionExpressionNode sum;
			sum = new AdditionExpressionNode(expr, true);

			boolean positive = lookahead.sequence.equals("+");
			nextToken();
			ExpressionNode t = term();
			sum.add(t, positive);

			return sumOp(sum);
		}

		__print("sum_op -> EPSILON");
		// sum_op -> EPSILON
		return expr;
	}

	private ExpressionNode term() {
		__print("term -> factor term_op");
		// term -> factor term_op
		ExpressionNode factor = factor();
		return termOp(factor);
	}

	private ExpressionNode termOp(ExpressionNode expression) {
		if (lookahead.tokenType == Token.MULT) {
			__print("term_op -> MULTDIV signed_factor term_op");

			MultiplicationExpressionNode prod;
			prod = new MultiplicationExpressionNode(expression, true);

			nextToken();
			ExpressionNode f = signedFactor();
			prod.add(f, true);

			return termOp(prod);
		}
		
		if (lookahead.tokenType == Token.DIV) {
			__print("term_op -> MULTDIV signed_factor term_op");

			MultiplicationExpressionNode prod;
			prod = new MultiplicationExpressionNode(expression, true);

			nextToken();
			ExpressionNode f = signedFactor();
			prod.add(f, false);

			return termOp(prod);
		}

		__print("term_op -> EPSILON");
		// term_op -> EPSILON
		return expression;
	}

	private ExpressionNode signedFactor() {
		if (lookahead.tokenType == Token.PLUSMINUS) {
			__print("signed_factor -> PLUSMINUS factor");
			// signed_factor -> PLUSMINUS factor
			boolean positive = lookahead.sequence.equals("+");
			nextToken();
			ExpressionNode expr = factor();
			if (positive) {
				return expr;
			} else {
				return new AdditionExpressionNode(expr, false);
			}
		}

		__print("signed_factor -> factor");
		// signed_factor -> factor
		return factor();
	}

	private ExpressionNode factor() {
		__print("factor -> argument factor_op");
		// factor -> argument factor_op
		ExpressionNode a = argument();
		return factorOp(a);
	}

	private ExpressionNode factorOp(ExpressionNode expression) {
		if (lookahead.tokenType == Token.RAISED) {
			__print("factor_op -> RAISED expression");
			// factor_op -> RAISED expression
			nextToken();
			ExpressionNode exponent = signedFactor();
			return new ExponentiationExpressionNode(expression, exponent);
		}
		__print("factor_op -> EPSILON");
		// factor_op -> EPSILON
		return expression;
	}

	private ExpressionNode argument() {
		if (lookahead.tokenType == Token.FUNCTION) {
			__print("argument -> FUNCTION argument");
			// argument -> FUNCTION argument
			int function = FunctionExpressionNode.stringToFunction(lookahead.sequence);
			nextToken();
			ExpressionNode expr = argument();
			return new FunctionExpressionNode(function, expr);
		}

		if (lookahead.tokenType == Token.OPEN_BRACKET) {
			__print("argument -> OPEN_BRACKET expression CLOSE_BRACKET");
			// argument -> OPEN_BRACKET expression CLOSE_BRACKET
			nextToken();
			ExpressionNode expr = expression();

			if (lookahead.tokenType != Token.CLOSE_BRACKET) {
				throw new ParserException("Close brackets expected, " + lookahead.sequence
						+ " found instead!");
			}

			nextToken();
			return expr;
		}

		__print("argument -> value");
		// argument -> value
		return value();
	}

	private ExpressionNode value() {
		if (lookahead.tokenType == Token.NUMBER) {
			__print("value -> NUMBER");
			// value -> NUMBER
			ExpressionNode expr = new ConstantExpressionNode(lookahead.sequence);
			nextToken();
			return expr;
		}

		if (lookahead.tokenType == Token.VARIABLE) {
			__print("value -> VARIABLE");
			// value -> VARIABLE
			ExpressionNode expr = new VariableExpressionNode(lookahead.sequence);
			nextToken();
			return expr;
		}

		if (lookahead.tokenType == Token.EPSILON)
			throw new ParserException("Unexpected end of input");
		else
			throw new ParserException("Unexpected symbol %s found", lookahead);
	}

	private ExpressionNode signedTerm() {
		// there are two possible rules. If the next token is PLUSMINUS we can
		// eat it up and then parse the non-terminal term. Otherwise we parse
		// the non-terminal term directly

		if (lookahead.tokenType == Token.PLUSMINUS) {
			__print("signedTerm -> PLUSMINUS term");
			// signedTerm -> PLUSMINUS term
			boolean positive = lookahead.sequence.equals("+");
			nextToken();
			ExpressionNode expr = term();
			return new AdditionExpressionNode(expr, positive);
		}

		__print("signedTerm -> term");
		// signedTerm -> term
		return term();
	}
}
