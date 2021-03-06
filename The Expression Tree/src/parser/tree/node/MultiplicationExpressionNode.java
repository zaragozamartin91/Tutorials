package parser.tree.node;

public class MultiplicationExpressionNode extends SequenceExpressionNode {
	public MultiplicationExpressionNode() {
		super();
	}

	public MultiplicationExpressionNode(ExpressionNode a, boolean positive) {
		super(a, positive);
	}

	@Override
	public int getType() {
		return MULTIPLICATION_NODE;
	}

	@Override
	public double getValue() {
		double prod = 1.0;

		for (Term term : terms) {
			if (term.positive) {
				prod *= term.expression.getValue();
			} else {
				prod /= term.expression.getValue();
			}
		}

		return prod;
	}
}
