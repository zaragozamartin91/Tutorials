package parser;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import parser.token.Token;
import parser.token.Tokenizer;
import static parser.token.Token.*;

public class ParserTest {

	@Test
	public void testParse() {
		try {
			Tokenizer tokenizer = new Tokenizer();
			tokenizer.add("sin|cos|exp|ln|sqrt", FUNCTION); // function
			tokenizer.add("\\(", OPEN_BRACKET); // open bracket
			tokenizer.add("\\)", CLOSE_BRACKET); // close bracket
			tokenizer.add("[+-]", PLUSMINUS); // plus or minus
			tokenizer.add("[*/]", MULTDIV); // mult or divide
			tokenizer.add("\\^", RAISED); // raised
			tokenizer.add("[0-9]+", NUMBER); // integer number
			tokenizer.add("[a-zA-Z][a-zA-Z0-9_]*", VARIABLE); // variable

			tokenizer.tokenize(" sin(x) * (1 + var_12) ");

			LinkedList<Token> tokens = tokenizer.getTokens();
			for (Token token : tokens) {
				System.out.println(token.token + " : " + token.sequence);
			}
			
			Parser parser = new Parser();
			parser.parse(tokens);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
