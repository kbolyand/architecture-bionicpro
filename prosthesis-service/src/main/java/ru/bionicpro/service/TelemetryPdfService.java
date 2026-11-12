package ru.bionicpro.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import ru.bionicpro.model.TelemetryClientDaily;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TelemetryPdfService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generatePdf(List<TelemetryClientDaily> data) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate());

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    16
            );

            Font headerFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    8
            );

            Font bodyFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    7
            );

            Paragraph title = new Paragraph(
                    "Telemetry Client Daily Report",
                    titleFont
            );

            title.setSpacingAfter(15);

            document.add(title);

            PdfPTable table = new PdfPTable(13);

            table.setWidthPercentage(100);

            // Ширины колонок
            table.setWidths(new float[]{
                    7, 12, 12, 5, 7,
                    8, 8, 8,
                    8, 8, 8,
                    12, 12
            });

            addHeader(table, "Date", headerFont);
            addHeader(table, "User ID", headerFont);
            addHeader(table, "Prosthesis ID", headerFont);
            addHeader(table, "Channel", headerFont);
            addHeader(table, "Samples", headerFont);

            addHeader(table, "Avg Amp.", headerFont);
            addHeader(table, "Min Amp.", headerFont);
            addHeader(table, "Max Amp.", headerFont);

            addHeader(table, "Avg Freq.", headerFont);
            addHeader(table, "Min Freq.", headerFont);
            addHeader(table, "Max Freq.", headerFont);

            addHeader(table, "First Event", headerFont);
            addHeader(table, "Last Event", headerFont);

            for (TelemetryClientDaily item : data) {

                addCell(table, item.reportDate().toString(), bodyFont);
                addCell(table, item.userId().toString(), bodyFont);
                addCell(table, item.prosthesisId().toString(), bodyFont);
                addCell(table, String.valueOf(item.channel()), bodyFont);
                addCell(table, String.valueOf(item.samplesCount()), bodyFont);

                addCell(table, String.format("%.3f", item.avgAmplitude()), bodyFont);
                addCell(table, String.format("%.3f", item.minAmplitude()), bodyFont);
                addCell(table, String.format("%.3f", item.maxAmplitude()), bodyFont);

                addCell(table, String.format("%.3f", item.avgFrequency()), bodyFont);
                addCell(table, String.format("%.3f", item.minFrequency()), bodyFont);
                addCell(table, String.format("%.3f", item.maxFrequency()), bodyFont);

                addCell(
                        table,
                        item.firstEventTime().format(DATE_TIME_FORMATTER),
                        bodyFont
                );

                addCell(
                        table,
                        item.lastEventTime().format(DATE_TIME_FORMATTER),
                        bodyFont
                );
            }

            document.add(table);

            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to generate telemetry PDF",
                    e
            );
        }
    }

    private void addHeader(
            PdfPTable table,
            String value,
            Font font
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(value, font)
        );

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(cell);
    }

    private void addCell(
            PdfPTable table,
            String value,
            Font font
    ) {
        PdfPCell cell = new PdfPCell(
                new Phrase(value, font)
        );

        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(cell);
    }
}
