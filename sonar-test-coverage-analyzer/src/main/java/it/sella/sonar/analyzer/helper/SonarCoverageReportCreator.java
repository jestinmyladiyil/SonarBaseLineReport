package it.sella.sonar.analyzer.helper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import it.sella.sonar.analyzer.model.SonarProjectDetails;
import it.sella.sonar.analyzer.util.SonarAnalyzerUtil;
import it.sella.sonar.analyzer.util.SonarMetricMeasures;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SonarCoverageReportCreator {

	public void createBaseLineReport(List<SonarProjectDetails> projectDetails, String reportFolderPath) {
		log.debug("SonarCoverageReportCreator >> createBaseLineReport");
		String reportFile = reportFolderPath + getReportFileName();
		try {
			log.debug("creating required directories...");
			Files.createDirectories(Paths.get(reportFolderPath));
			Resource resource = new ClassPathResource("/baseline-values.properties");
			log.debug("extracting baseline values from property file...");
			Properties baseLineProps = PropertiesLoaderUtils.loadProperties(resource);
			createXLSReportFile(projectDetails, reportFile, baseLineProps);
		} catch (IOException ex) {

		}
	}

	private String getReportFileName() {
		log.debug("SonarCoverageReportCreator >> getReportFileName");
		return "Components_Coverage_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "_Java.xls";
	}

	protected static void createXLSReportFile(List<SonarProjectDetails> projectDetails, String reportFile,
			Properties baseLineProps) {
		log.debug("SonarCoverageReportCreator >> createXLSReportFile");
		try (FileOutputStream xlsFile = new FileOutputStream(reportFile); HSSFWorkbook workbook = new HSSFWorkbook()) {
			final HSSFSheet sheet = workbook.createSheet("Components Coverage");
			final HSSFRow rowhead = sheet.createRow(0);
			rowhead.createCell(Short.valueOf("0")).setCellValue(new HSSFRichTextString("Component Name"));
			rowhead.createCell(Short.valueOf("1")).setCellValue(new HSSFRichTextString("TYPE"));
			rowhead.createCell(Short.valueOf("2")).setCellValue(new HSSFRichTextString("Base Lines to cover"));
			rowhead.createCell(Short.valueOf("3")).setCellValue(new HSSFRichTextString("Base Covered Lines"));
			rowhead.createCell(Short.valueOf("4")).setCellValue(new HSSFRichTextString("Current Lines to cover"));
			rowhead.createCell(Short.valueOf("5")).setCellValue(new HSSFRichTextString("Current Covered Lines"));
			rowhead.createCell(Short.valueOf("6")).setCellValue(
					new HSSFRichTextString("Delta Lines to cover(Current Lines to cover - Base Lines to cover)"));
			rowhead.createCell(Short.valueOf("7")).setCellValue(
					new HSSFRichTextString("Delta Covered Lines (Current Covered Lines - Base Covered Lines)"));
			rowhead.createCell(Short.valueOf("8")).setCellValue(
					new HSSFRichTextString("Coverage Percentage (Delta Covered Lines / Delta Lines to cover) * 100"));
			applyheaderStyle(sheet.getRow(0), workbook);
			if (!projectDetails.isEmpty()) {
				int i = 1;
				HSSFRow row = null;
				for (SonarProjectDetails project : projectDetails) {
					row = sheet.createRow(i);
					setValues(row, project, baseLineProps);
					i++;
				}
			}
			workbook.write(xlsFile);
		} catch (final FileNotFoundException e) {
			log.error("FileNotFoundException occurred in createXLSReportFile: ", e);
		} catch (final IOException e) {
			log.error("IOException occurred in createXLSReportFile: ", e);
		}
	}

	private static void applyheaderStyle(HSSFRow row, HSSFWorkbook workbook) {
		log.debug("SonarCoverageReportCreator >> applyheaderStyle");
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setWrapText(Boolean.TRUE);
		HSSFFont defaultFont = workbook.createFont();
		defaultFont.setBold(Boolean.TRUE);
		headerStyle.setFont(defaultFont);
		for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
			row.getCell(cellNum).setCellStyle(headerStyle);
			workbook.getSheetAt(0).autoSizeColumn(cellNum);
		}
		log.debug("header styles applied...");
	}

	private static void setValues(HSSFRow row, SonarProjectDetails project, Properties baseLineProps) {
		log.debug("SonarCoverageReportCreator >> setValues");
		String baseLinesToCover = baseLineProps
				.getProperty(project.getName() + '_' + SonarMetricMeasures.LINES_TO_COVER.getValue());
		String baseLinesCovered = baseLineProps
				.getProperty(project.getName() + '_' + SonarMetricMeasures.COVERED_LINES.getValue());
		String currentLinesToCover = project.getMetricDataMap().get(SonarMetricMeasures.LINES_TO_COVER.getValue());
		String currentLinesCovered = project.getMetricDataMap().get(SonarMetricMeasures.COVERED_LINES.getValue());
		Double deltaLinesToCover = SonarAnalyzerUtil.getDouble(currentLinesToCover)
				- SonarAnalyzerUtil.getDouble(baseLinesToCover);
		Double deltaLinesCovered = SonarAnalyzerUtil.getDouble(currentLinesCovered)
				- SonarAnalyzerUtil.getDouble(baseLinesCovered);
		String coveredPercentage = findTestCoverage(baseLinesToCover, deltaLinesToCover, deltaLinesCovered);
		log.debug("setting values in cells...");
		row.createCell(Short.valueOf("0")).setCellValue(new HSSFRichTextString(project.getName()));
		row.createCell(Short.valueOf("1")).setCellValue(new HSSFRichTextString("EAR"));
		row.createCell(Short.valueOf("2")).setCellValue(new HSSFRichTextString(baseLinesToCover));
		row.createCell(Short.valueOf("3")).setCellValue(new HSSFRichTextString(baseLinesCovered));
		row.createCell(Short.valueOf("4")).setCellValue(new HSSFRichTextString(currentLinesToCover));
		row.createCell(Short.valueOf("5")).setCellValue(new HSSFRichTextString(currentLinesCovered));
		row.createCell(Short.valueOf("6")).setCellValue(new HSSFRichTextString(String.valueOf(deltaLinesToCover)));
		row.createCell(Short.valueOf("7")).setCellValue(new HSSFRichTextString(String.valueOf(deltaLinesCovered)));
		row.createCell(Short.valueOf("8")).setCellValue(new HSSFRichTextString(String.valueOf(coveredPercentage)));
	}

	private static String findTestCoverage(String baseLinesToCover, Double deltaLinesToCover,
			Double deltaLinesCovered) {
		log.debug("SonarCoverageReportCreator >> findTestCoverage");
		String coveragePercentage = "0".equals(baseLinesToCover) ? "NA" : "0";
		if (0 != deltaLinesToCover) {
			log.debug("finding coverage percentage...");
			coveragePercentage = String.valueOf(Math.round((deltaLinesCovered / deltaLinesToCover) * 100));
		}
		return coveragePercentage;
	}
}
