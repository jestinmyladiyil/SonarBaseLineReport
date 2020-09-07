package it.sella.sonar.analyzer.util;

public class SonarAnalyzerUtil {

	private SonarAnalyzerUtil() {
	}

	public static double getDouble(String input) {
		double value = 0;
		try {
			value = Double.parseDouble(input);
		} catch (NumberFormatException ex) {

		}
		return value;
	}
}
