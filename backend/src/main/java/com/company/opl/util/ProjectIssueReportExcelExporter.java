package com.company.opl.util;

import com.company.opl.entity.Issue;
import com.company.opl.entity.IssueAttachment;
import com.company.opl.entity.Project;
import com.company.opl.enums.IssuePriorityEnum;
import com.company.opl.enums.IssueStatusEnum;
import com.company.opl.query.issue.IssueQuery;
import com.company.opl.vo.project.ProjectIssueSummaryVO;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ProjectIssueReportExcelExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ProjectIssueReportExcelExporter() {
    }

    public static byte[] export(Project project,
                                ProjectIssueSummaryVO summary,
                                List<Issue> issues,
                                IssueQuery query,
                                String ownerFilterLabel,
                                Map<Long, List<IssueAttachment>> attachmentMap,
                                String publicBaseUrl) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ReportStyles styles = createStyles(workbook);
            buildCoverSheet(workbook, project, summary, issues, styles);
            buildSummarySheet(workbook, project, summary, issues, query, ownerFilterLabel, styles);
            buildChartsSheet(workbook, summary, issues, styles);
            buildDetailSheet(workbook, issues, attachmentMap, publicBaseUrl, styles);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to export issue report", exception);
        }
    }

    private static void buildCoverSheet(XSSFWorkbook workbook,
                                        Project project,
                                        ProjectIssueSummaryVO summary,
                                        List<Issue> issues,
                                        ReportStyles styles) {
        Sheet sheet = workbook.createSheet("报表封面");
        sheet.setDisplayGridlines(false);
        for (int index = 0; index < 8; index++) {
            sheet.setColumnWidth(index, 18 * 256);
        }
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 7));
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 7));
        sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 7));
        sheet.addMergedRegion(new CellRangeAddress(17, 18, 0, 7));

        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(34);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Project Issue Hub Report");
        titleCell.setCellStyle(styles.coverTitleStyle());

        Row projectRow = sheet.createRow(4);
        Cell projectCell = projectRow.createCell(0);
        projectCell.setCellValue(nullSafe(project.getProjectName()) + " / " + nullSafe(project.getProjectNo()));
        projectCell.setCellStyle(styles.coverSubtitleStyle());

        Row subRow = sheet.createRow(6);
        Cell subCell = subRow.createCell(0);
        subCell.setCellValue("现场问题协同系统项目报告");
        subCell.setCellStyle(styles.coverHintStyle());

        createLabelValueRow(sheet, 9, "客户", nullSafe(project.getCustomerName()), "项目经理", nullSafe(project.getProjectManagerName()), styles.labelStyle(), styles.valueStyle());
        createLabelValueRow(sheet, 10, "项目状态", resolveProjectStatus(project.getStatus()), "导出时间", formatDateTime(LocalDateTime.now()), styles.labelStyle(), styles.valueStyle());
        createLabelValueRow(sheet, 11, "开始日期", formatDate(project.getStartDate()), "计划结束", formatDate(project.getPlannedEndDate()), styles.labelStyle(), styles.valueStyle());
        createLabelValueRow(sheet, 12, "项目问题总数", String.valueOf(summary.getTotalIssues()), "本次导出明细", String.valueOf(issues.size()), styles.labelStyle(), styles.valueStyle());
        createLabelValueRow(sheet, 13, "未关闭", String.valueOf(summary.getOpenIssues()), "已关闭", String.valueOf(summary.getClosedIssues()), styles.labelStyle(), styles.valueStyle());
        createLabelValueRow(sheet, 14, "处理中/待验证", String.valueOf(summary.getProcessingIssues()), "已超期", String.valueOf(summary.getOverdueIssues()), styles.labelStyle(), styles.valueStyle());

        Row noteRow = sheet.createRow(17);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("说明：本报表包含项目概览、统计图表以及问题清单明细，可用于项目例会、管理汇报和问题追溯。");
        noteCell.setCellStyle(styles.coverNoteStyle());
    }

    private static void buildSummarySheet(XSSFWorkbook workbook,
                                          Project project,
                                          ProjectIssueSummaryVO summary,
                                          List<Issue> issues,
                                          IssueQuery query,
                                          String ownerFilterLabel,
                                          ReportStyles styles) {
        Sheet sheet = workbook.createSheet("项目概览");
        sheet.setDefaultColumnWidth(18);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Project Issue Hub Report");
        titleCell.setCellStyle(styles.titleStyle());

        int rowIndex = 2;
        rowIndex = createSectionTitle(sheet, rowIndex, "项目基本信息", styles.sectionStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "项目编号", nullSafe(project.getProjectNo()), "项目名称", nullSafe(project.getProjectName()), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "客户", nullSafe(project.getCustomerName()), "项目经理", nullSafe(project.getProjectManagerName()), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "项目状态", resolveProjectStatus(project.getStatus()), "导出时间", formatDateTime(LocalDateTime.now()), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "开始日期", formatDate(project.getStartDate()), "计划结束", formatDate(project.getPlannedEndDate()), styles.labelStyle(), styles.valueStyle());

        rowIndex++;
        rowIndex = createSectionTitle(sheet, rowIndex, "项目统计信息", styles.sectionStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "问题总数", String.valueOf(summary.getTotalIssues()), "未关闭", String.valueOf(summary.getOpenIssues()), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "高优先级", String.valueOf(summary.getHighPriorityIssues()), "处理中/待验证", String.valueOf(summary.getProcessingIssues()), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "待分派", String.valueOf(summary.getUnassignedIssues()), "超期", String.valueOf(summary.getOverdueIssues()), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "已关闭", String.valueOf(summary.getClosedIssues()), "本次导出明细数", String.valueOf(issues.size()), styles.labelStyle(), styles.valueStyle());

        rowIndex++;
        rowIndex = createSectionTitle(sheet, rowIndex, "导出条件", styles.sectionStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "关键字", nullSafe(query.getKeyword()), "责任人", nullSafe(ownerFilterLabel), styles.labelStyle(), styles.valueStyle());
        rowIndex = createLabelValueRow(sheet, rowIndex, "状态", resolveStatusFilters(query.getStatusList()), "优先级", resolvePriority(query.getPriority()), styles.labelStyle(), styles.valueStyle());
        createLabelValueRow(sheet, rowIndex, "仅看超期", Boolean.TRUE.equals(query.getOverdueOnly()) ? "是" : "否", "导出说明", "统计口径按整个项目，明细按当前筛选条件导出。", styles.labelStyle(), styles.valueStyle());
    }

    private static void buildChartsSheet(XSSFWorkbook workbook,
                                         ProjectIssueSummaryVO summary,
                                         List<Issue> issues,
                                         ReportStyles styles) {
        XSSFSheet sheet = workbook.createSheet("统计图表");
        for (int index = 0; index < 20; index++) {
            sheet.setColumnWidth(index, 14 * 256);
        }

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("项目统计图表");
        titleCell.setCellStyle(styles.titleStyle());

        int metricStartRow = 3;
        Row metricHeader = sheet.createRow(metricStartRow);
        writeCell(metricHeader, 0, "指标", styles.tableHeaderStyle());
        writeCell(metricHeader, 1, "数量", styles.tableHeaderStyle());

        String[] metricLabels = {"问题总数", "未关闭", "高优先级", "处理中/待验证", "待分派", "已超期", "已关闭"};
        int[] metricValues = {
                summary.getTotalIssues(),
                summary.getOpenIssues(),
                summary.getHighPriorityIssues(),
                summary.getProcessingIssues(),
                summary.getUnassignedIssues(),
                summary.getOverdueIssues(),
                summary.getClosedIssues()
        };
        for (int index = 0; index < metricLabels.length; index++) {
            Row row = sheet.createRow(metricStartRow + 1 + index);
            writeCell(row, 0, metricLabels[index], styles.tableCellStyle());
            writeNumberCell(row, 1, metricValues[index], styles.tableCellStyle());
        }

        int statusStartRow = 14;
        Row statusHeader = sheet.createRow(statusStartRow);
        writeCell(statusHeader, 0, "状态", styles.tableHeaderStyle());
        writeCell(statusHeader, 1, "当前导出数量", styles.tableHeaderStyle());
        Map<String, Integer> statusDistribution = buildStatusDistribution(issues);
        int statusRow = statusStartRow + 1;
        for (Map.Entry<String, Integer> entry : statusDistribution.entrySet()) {
            Row row = sheet.createRow(statusRow++);
            writeCell(row, 0, entry.getKey(), styles.tableCellStyle());
            writeNumberCell(row, 1, entry.getValue(), styles.tableCellStyle());
        }

        int priorityStartRow = 14;
        Row priorityHeader = sheet.getRow(priorityStartRow);
        writeCell(priorityHeader, 4, "优先级", styles.tableHeaderStyle());
        writeCell(priorityHeader, 5, "当前导出数量", styles.tableHeaderStyle());
        Map<String, Integer> priorityDistribution = buildPriorityDistribution(issues);
        int priorityRow = priorityStartRow + 1;
        for (Map.Entry<String, Integer> entry : priorityDistribution.entrySet()) {
            Row row = sheet.getRow(priorityRow);
            if (row == null) {
                row = sheet.createRow(priorityRow);
            }
            writeCell(row, 4, entry.getKey(), styles.tableCellStyle());
            writeNumberCell(row, 5, entry.getValue(), styles.tableCellStyle());
            priorityRow++;
        }

        createBarChart(sheet, "项目 KPI", 3, 0, 9, 1, 3, 3, 10, 19);
        createBarChart(sheet, "状态分布", 14, 0, statusRow - 1, 1, 11, 3, 18, 19);
        createBarChart(sheet, "优先级分布", 14, 4, priorityRow - 1, 5, 11, 21, 18, 37);
    }

    private static void buildDetailSheet(XSSFWorkbook workbook,
                                         List<Issue> issues,
                                         Map<Long, List<IssueAttachment>> attachmentMap,
                                         String publicBaseUrl,
                                         ReportStyles styles) {
        Sheet sheet = workbook.createSheet("问题清单");
        String[] headers = {
                "问题编号", "OPL编号", "标题", "描述", "状态", "优先级", "影响等级", "提报人", "责任人",
                "责任部门", "问题分类", "问题来源", "设备", "模块", "发生时间", "截止时间",
                "关闭时间", "关闭人", "关闭说明", "关闭证据", "最新动作时间", "创建时间", "附件数", "附件入口"
        };

        Row headerRow = sheet.createRow(0);
        for (int index = 0; index < headers.length; index++) {
            Cell cell = headerRow.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(styles.tableHeaderStyle());
        }

        int rowIndex = 1;
        for (Issue issue : issues) {
            Row row = sheet.createRow(rowIndex++);
            int column = 0;
            writeCell(row, column++, nullSafe(issue.getIssueNo()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getOplNo()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getTitle()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getDescription()), styles.tableCellStyle());
            writeCell(row, column++, resolveStatus(issue.getStatus()), styles.tableCellStyle());
            writeCell(row, column++, resolvePriorityValue(issue.getPriority()), styles.tableCellStyle());
            writeCell(row, column++, resolveImpactLevel(issue.getImpactLevel()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getReporterName()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getOwnerName()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getOwnerDepartmentName()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getCategoryCode()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getSourceCode()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getDeviceName()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getModuleName()), styles.tableCellStyle());
            writeCell(row, column++, formatDateTime(issue.getOccurredAt()), styles.tableCellStyle());
            writeCell(row, column++, formatDateTime(issue.getDueAt()), styles.tableCellStyle());
            writeCell(row, column++, formatDateTime(issue.getClosedAt()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getClosedByName()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getCloseReason()), styles.tableCellStyle());
            writeCell(row, column++, nullSafe(issue.getCloseEvidence()), styles.tableCellStyle());
            writeCell(row, column++, formatDateTime(issue.getLastFollowUpAt()), styles.tableCellStyle());
            writeCell(row, column++, formatDateTime(issue.getCreatedAt()), styles.tableCellStyle());

            List<IssueAttachment> attachments = attachmentMap.getOrDefault(issue.getId(), Collections.emptyList());
            writeNumberCell(row, column++, attachments.size(), styles.tableCellStyle());
            writeAttachmentLinkCell(workbook, row, column, attachments, publicBaseUrl, styles.linkStyle(), styles.tableCellStyle());
        }

        sheet.createFreezePane(0, 1);
        for (int index = 0; index < headers.length; index++) {
            int width = switch (index) {
                case 2, 3, 18, 19, 23 -> 30 * 256;
                default -> 18 * 256;
            };
            sheet.setColumnWidth(index, width);
        }
    }

    private static int createSectionTitle(Sheet sheet, int rowIndex, String title, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        return rowIndex + 1;
    }

    private static int createLabelValueRow(Sheet sheet, int rowIndex, String leftLabel, String leftValue, String rightLabel, String rightValue, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowIndex);
        writeCell(row, 0, leftLabel, labelStyle);
        writeCell(row, 1, leftValue, valueStyle);
        writeCell(row, 3, rightLabel, labelStyle);
        writeCell(row, 4, rightValue, valueStyle);
        return rowIndex + 1;
    }

    private static void writeCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void writeNumberCell(Row row, int columnIndex, int value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private static void writeAttachmentLinkCell(XSSFWorkbook workbook,
                                                Row row,
                                                int columnIndex,
                                                List<IssueAttachment> attachments,
                                                String publicBaseUrl,
                                                CellStyle linkStyle,
                                                CellStyle fallbackStyle) {
        if (attachments == null || attachments.isEmpty()) {
            writeCell(row, columnIndex, "-", fallbackStyle);
            return;
        }
        IssueAttachment firstAttachment = attachments.get(0);
        String address = normalizeBaseUrl(publicBaseUrl) + "/api/attachments/" + firstAttachment.getId() + "/content";
        String label = attachments.size() == 1
                ? "查看附件：" + nullSafe(firstAttachment.getFileName())
                : "查看首个附件（共 " + attachments.size() + " 个）";

        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(label);
        cell.setCellStyle(linkStyle);
        CreationHelper creationHelper = workbook.getCreationHelper();
        Cell hyperlinkCell = cell;
        hyperlinkCell.setHyperlink(creationHelper.createHyperlink(HyperlinkType.URL));
        hyperlinkCell.getHyperlink().setAddress(address);
    }

    private static void createBarChart(XSSFSheet sheet,
                                       String title,
                                       int categoryStartRow,
                                       int categoryColumn,
                                       int categoryEndRow,
                                       int valueColumn,
                                       int anchorCol1,
                                       int anchorRow1,
                                       int anchorCol2,
                                       int anchorRow2) {
        if (categoryEndRow < categoryStartRow + 1) {
            return;
        }
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, anchorCol1, anchorRow1, anchorCol2, anchorRow2);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);
        chart.getOrAddLegend().setPosition(LegendPosition.RIGHT);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        var categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(categoryStartRow + 1, categoryEndRow, categoryColumn, categoryColumn));
        XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(categoryStartRow + 1, categoryEndRow, valueColumn, valueColumn));
        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFChartData.Series series = data.addSeries(categories, values);
        series.setTitle("数量", null);
        ((XDDFBarChartData) data).setBarDirection(BarDirection.COL);
        chart.plot(data);
    }

    private static Map<String, Integer> buildStatusDistribution(List<Issue> issues) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (IssueStatusEnum item : List.of(
                IssueStatusEnum.NEW,
                IssueStatusEnum.ACCEPTED,
                IssueStatusEnum.IN_PROGRESS,
                IssueStatusEnum.PENDING_VERIFY,
                IssueStatusEnum.CLOSED,
                IssueStatusEnum.ON_HOLD,
                IssueStatusEnum.CANCELED)) {
            counts.put(item.getDescription(), 0);
        }
        for (Issue issue : issues) {
            String key = resolveStatus(issue.getStatus());
            counts.computeIfPresent(key, (ignore, value) -> value + 1);
        }
        return counts;
    }

    private static Map<String, Integer> buildPriorityDistribution(List<Issue> issues) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("低", 0);
        counts.put("中", 0);
        counts.put("高", 0);
        counts.put("紧急", 0);
        for (Issue issue : issues) {
            String key = resolvePriorityValue(issue.getPriority());
            counts.computeIfPresent(key, (ignore, value) -> value + 1);
        }
        return counts;
    }

    private static ReportStyles createStyles(XSSFWorkbook workbook) {
        return new ReportStyles(
                createTitleStyle(workbook),
                createSectionStyle(workbook),
                createLabelStyle(workbook),
                createValueStyle(workbook),
                createTableHeaderStyle(workbook),
                createTableCellStyle(workbook),
                createLinkStyle(workbook),
                createCoverTitleStyle(workbook),
                createCoverSubtitleStyle(workbook),
                createCoverHintStyle(workbook),
                createCoverNoteStyle(workbook)
        );
    }

    private static CellStyle createTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 15);
        style.setFont(font);
        return style;
    }

    private static CellStyle createSectionStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createLabelStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createValueStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private static CellStyle createTableHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        addBorder(style);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle createTableCellStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        addBorder(style);
        return style;
    }

    private static CellStyle createLinkStyle(XSSFWorkbook workbook) {
        CellStyle style = createTableCellStyle(workbook);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setUnderline(Font.U_SINGLE);
        style.setFont(font);
        return style;
    }

    private static CellStyle createCoverTitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 22);
        style.setFont(font);
        return style;
    }

    private static CellStyle createCoverSubtitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private static CellStyle createCoverHintStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        return style;
    }

    private static CellStyle createCoverNoteStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private static void addBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private static String normalizeBaseUrl(String publicBaseUrl) {
        String baseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl.isBlank() ? "http://localhost" : baseUrl;
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME_FORMATTER.format(value);
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "-" : DATE_FORMATTER.format(value);
    }

    private static String resolveStatus(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        try {
            return IssueStatusEnum.fromCode(value).getDescription();
        } catch (IllegalArgumentException exception) {
            return value;
        }
    }

    private static String resolveStatusFilters(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "全部";
        }
        return values.stream().filter(Objects::nonNull).map(ProjectIssueReportExcelExporter::resolveStatus).collect(Collectors.joining(" / "));
    }

    private static String resolvePriority(String value) {
        if (value == null || value.isBlank()) {
            return "全部";
        }
        return resolvePriorityValue(value);
    }

    private static String resolvePriorityValue(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return switch (IssuePriorityEnum.valueOf(value)) {
            case LOW -> "低";
            case MEDIUM -> "中";
            case HIGH -> "高";
            case CRITICAL -> "紧急";
        };
    }

    private static String resolveImpactLevel(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return switch (value) {
            case "LOW" -> "低";
            case "MEDIUM" -> "中";
            case "HIGH" -> "高";
            case "CRITICAL" -> "严重";
            default -> value;
        };
    }

    private static String resolveProjectStatus(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return switch (value) {
            case "PLANNING" -> "规划中";
            case "IN_PROGRESS" -> "进行中";
            case "DELIVERING" -> "交付中";
            case "CLOSED" -> "已关闭";
            case "CANCELED" -> "已取消";
            default -> value;
        };
    }

    private record ReportStyles(CellStyle titleStyle, CellStyle sectionStyle, CellStyle labelStyle, CellStyle valueStyle, CellStyle tableHeaderStyle, CellStyle tableCellStyle, CellStyle linkStyle, CellStyle coverTitleStyle, CellStyle coverSubtitleStyle, CellStyle coverHintStyle, CellStyle coverNoteStyle) {
    }
}
