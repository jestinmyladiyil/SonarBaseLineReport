package it.sella.sonar.analyzer.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SonarCoverageMetrics {

	private String key;

	private String name;

	private List<SonarCoverageMeasures> measures;

}
