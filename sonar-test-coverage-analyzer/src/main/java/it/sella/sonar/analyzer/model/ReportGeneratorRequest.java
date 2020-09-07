package it.sella.sonar.analyzer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportGeneratorRequest {
	
	public ReportGeneratorRequest(boolean baseline) {
		this.baseLineReport = baseline;
	}

	private String host;

	private String port;

	private boolean credentialsProvided;

	private String user;

	private String password;

	private String token;

	private boolean baseLineReport;

	private String reportFolderPath;

}
