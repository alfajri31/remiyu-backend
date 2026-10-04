package id.remiyu.backend.support.importchat;

import static org.assertj.core.api.Assertions.*;

import id.remiyu.backend.common.exception.ApiException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ImportFileReaderTest {
  final ImportFileReader reader = new ImportFileReader();

  byte[] zip(String... entries) throws Exception {
    var out = new ByteArrayOutputStream();
    try (var zip = new ZipOutputStream(out)) {
      for (String entry : entries) {
        zip.putNextEntry(new ZipEntry(entry));
        zip.write("chat".getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
      }
    }
    return out.toByteArray();
  }

  @Test
  void acceptsZipWithOneTextFile() throws Exception {
    assertThat(
            reader.read(
                new MockMultipartFile(
                    "file", "chat.zip", "application/zip", zip("folder/chat.txt"))))
        .isEqualTo("chat");
  }

  @Test
  void rejectsMultipleChatFilesAndInvalidUtf8() throws Exception {
    assertThatThrownBy(
            () ->
                reader.read(
                    new MockMultipartFile(
                        "file", "chat.zip", "application/zip", zip("a.txt", "b.txt"))))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(
            () ->
                reader.read(
                    new MockMultipartFile(
                        "file", "chat.txt", "text/plain", new byte[] {(byte) 0xFF})))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void limitsInflatedZipContent() throws Exception {
    var out = new ByteArrayOutputStream();
    try (var zip = new ZipOutputStream(out)) {
      zip.putNextEntry(new ZipEntry("chat.txt"));
      zip.write(new byte[1024 * 1024 + 1]);
      zip.closeEntry();
    }
    assertThatThrownBy(
            () ->
                reader.read(
                    new MockMultipartFile(
                        "file", "chat.zip", "application/zip", out.toByteArray())))
        .isInstanceOf(ApiException.class);
  }
}
