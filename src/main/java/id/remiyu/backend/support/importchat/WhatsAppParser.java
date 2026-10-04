package id.remiyu.backend.support.importchat;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.model.ParsedChatMessage;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppParser {
  // Indonesian day/month convention; ISO dates also accepted.
  private static final Pattern LINE =
      Pattern.compile(
          "^\\[?(\\d{1,4}[/.-]\\d{1,2}[/.-]\\d{1,4}),?\\s+(\\d{1,2}[:.]\\d{2}(?:[:.]\\d{2})?(?:\\s*[APap][Mm])?)\\]?\\s*(?:-\\s*)?(.+)$");

  public List<ParsedChatMessage> parse(String input) {
    var result = new ArrayList<ParsedChatMessage>();
    for (String raw :
        input.replace("\uFEFF", "").replace("\u200E", "").replace("\u202F", " ").split("\\R")) {
      var match = LINE.matcher(raw);
      if (match.matches()) {
        int colon = match.group(3).indexOf(": ");
        if (colon < 1) continue; // platform system notices have no sender
        String sender = match.group(3).substring(0, colon).trim();
        String text = match.group(3).substring(colon + 2).trim();
        if (sender.length() > 254) throw ApiException.badRequest("Nama pengirim terlalu panjang.");
        result.add(new ParsedChatMessage(sender, time(match.group(1), match.group(2)), text));
      } else if (!result.isEmpty() && !raw.isBlank()) {
        var last = result.removeLast();
        result.add(new ParsedChatMessage(last.sender(), last.sentAt(), last.text() + "\n" + raw));
      }
    }
    if (result.isEmpty())
      throw ApiException.badRequest(
          "Tidak ditemukan pesan chat. Gunakan export WhatsApp .txt atau .zip berisi .txt.");
    if (result.size() > 1000)
      throw ApiException.badRequest(
          "Pilot dibatasi 1.000 pesan per import; pecah file menjadi bagian lebih kecil.");
    if (result.stream().anyMatch(m -> m.text().length() > 16000))
      throw ApiException.badRequest("Satu pesan maksimal 16.000 karakter.");
    return result;
  }

  private OffsetDateTime time(String date, String clock) {
    try {
      String[] d = date.split("[/.-]");
      int day, month = Integer.parseInt(d[1]), year;
      if (d[0].length() == 4) {
        year = Integer.parseInt(d[0]);
        day = Integer.parseInt(d[2]);
      } else {
        day = Integer.parseInt(d[0]);
        year = Integer.parseInt(d[2]);
        if (year < 100) year += 2000;
      }
      clock = clock.replace('.', ':').toUpperCase(Locale.ROOT).replaceAll("\\s*([AP]M)$", " $1");
      DateTimeFormatter fmt =
          new DateTimeFormatterBuilder()
              .parseCaseInsensitive()
              .appendPattern(clock.contains("M") ? "h:mm[:ss] a" : "H:mm[:ss]")
              .toFormatter(Locale.US);
      return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.parse(clock, fmt))
          .atZone(ZoneId.of("Asia/Jakarta"))
          .toOffsetDateTime();
    } catch (DateTimeException | NumberFormatException e) {
      throw ApiException.badRequest("Tanggal chat tidak valid. Gunakan format hari/bulan/tahun.");
    }
  }
}
