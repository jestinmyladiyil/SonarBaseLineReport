package it.sella.sonar.analyzer.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SonarProjectDetails {

	private String name;

	private String key;
	
	private Map<String,String> metricDataMap;

}
