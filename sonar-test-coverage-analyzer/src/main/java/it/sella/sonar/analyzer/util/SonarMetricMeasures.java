package it.sella.sonar.analyzer.util;

public enum SonarMetricMeasures {

	LINES_TO_COVER("lines_to_cover"), UNCOVERED_LINES("uncovered_lines"), COVERED_LINES("covered_lines");

	private String measure;

	SonarMetricMeasures(String measure) {
		this.measure = measure;
	}

	public String getValue() {
		return this.measure;
	}

}
