package uk.co.unitycoders.pircbotx.commands.math;

public class MathFunction implements Function {
	private String name;
	private boolean leftAssoc;
	private int precidence;
	
	public MathFunction(String name, boolean leftAssoc, int precidence) {
		this.name = name;
		this.leftAssoc = leftAssoc;
		this.precidence = precidence;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getArity() {
		return 1;
	}

	@Override
	public double evaulate(double... args) {
		
		if ("SIN".equals(name)) {
			return Math.sin(args[0]);
		}
		
		if ("TAN".equals(name)) {
			return Math.tan(args[0]);
		}
		
		if ("COS".equals(name)) {
			return Math.cos(args[0]);
		}
		
		if ("DEG".equals(name)) {
			return Math.toDegrees(args[0]);
		}
		
		if ("RAD".equals(name)) {
			return Math.toRadians(args[0]);
		}
		
		return 0;
	}

	@Override
	public boolean isLeftAssoc() {
		return leftAssoc;
	}

	@Override
	public int getPrecidence() {
		return precidence;
	}

}
