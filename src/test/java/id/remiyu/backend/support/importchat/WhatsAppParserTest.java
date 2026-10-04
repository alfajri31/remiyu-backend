package id.remiyu.backend.support.importchat;

import static org.assertj.core.api.Assertions.*;

import id.remiyu.backend.common.exception.ApiException;
import org.junit.jupiter.api.Test;

class WhatsAppParserTest {
  final WhatsAppParser parser = new WhatsAppParser();

  @Test
  void parsesAndroidMultilineAndSkipsSystemNotices() {
    var messages =
        parser.parse(
            "04/10/2026, 09:00 - Messages are encrypted\n04/10/2026, 09:01 - Budi: Proposal\nbaris kedua");
    assertThat(messages).hasSize(1);
    assertThat(messages.getFirst().text()).isEqualTo("Proposal\nbaris kedua");
    assertThat(messages.getFirst().sentAt().getMonthValue()).isEqualTo(10);
  }

  @Test
  void parsesIosAndTwelveHourClock() {
    var messages = parser.parse("[04/10/26, 9:01:03 PM] Sari: Rapat");
    assertThat(messages.getFirst().sentAt().getHour()).isEqualTo(21);
    assertThat(messages.getFirst().sentAt().getYear()).isEqualTo(2026);
  }

  @Test
  void rejectsEmptyOrInvalidDate() {
    assertThatThrownBy(() -> parser.parse("random file")).isInstanceOf(ApiException.class);
    assertThatThrownBy(() -> parser.parse("31/02/26, 09:00 - Budi: Rapat"))
        .isInstanceOf(ApiException.class);
  }
}
