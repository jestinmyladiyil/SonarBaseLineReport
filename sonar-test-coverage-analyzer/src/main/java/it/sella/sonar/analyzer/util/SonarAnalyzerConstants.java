package it.sella.sonar.analyzer.util;

import it.sella.sonar.analyzer.model.ReportGeneratorRequest;

public class SonarAnalyzerConstants {

	private static final String API_PROJECT_LIST = "/api/components/search?qualifiers=TRK";

	private static final String API_METRIC_DATA = "/api/measures/component?componentKey={projectKey}&metricKeys=lines_to_cover,uncovered_lines";

	public static String getURLForProjectList(ReportGeneratorRequest request) {
		return "http://" + request.getHost() + ":" + request.getPort() + API_PROJECT_LIST;
	}

	public static String getURLForMetricData(ReportGeneratorRequest request, String projectKey) {
		return ("http://" + request.getHost() + ":" + request.getPort() + API_METRIC_DATA).replace("{projectKey}",
				projectKey);
	}
}
