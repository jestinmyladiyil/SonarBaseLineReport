package it.sella.sonar.analyzer.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SonarProjectMetricResponse {

	private SonarCoverageMetrics component;

}
