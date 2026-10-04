package id.remiyu.backend.support.importchat;

import id.remiyu.backend.common.exception.ApiException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.Locale;
import java.util.zip.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImportFileReader {
  private static final int MAX_BYTES = 1024 * 1024;

  public String read(MultipartFile file) {
    if (file.isEmpty() || file.getSize() > MAX_BYTES)
      throw ApiException.badRequest("File harus berisi data dan maksimal 1 MB.");
    String name =
        file.getOriginalFilename() == null
            ? ""
            : file.getOriginalFilename().toLowerCase(Locale.ROOT);
    try {
      byte[] bytes;
      if (name.endsWith(".txt")) bytes = file.getBytes();
      else if (name.endsWith(".zip")) {
        bytes = null;
        int entries = 0, totalBytes = 0;
        try (var zip = new ZipInputStream(file.getInputStream())) {
          ZipEntry entry;
          while ((entry = zip.getNextEntry()) != null) {
            if (++entries > 100) throw ApiException.badRequest("ZIP maksimal 100 entri.");
            byte[] entryBytes = zip.readNBytes(MAX_BYTES - totalBytes + 1);
            totalBytes += entryBytes.length;
            if (totalBytes > MAX_BYTES)
              throw ApiException.badRequest("Total isi ZIP maksimal 1 MB.");
            if (!entry.isDirectory() && entry.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
              if (bytes != null)
                throw ApiException.badRequest("ZIP harus berisi tepat satu file chat .txt.");
              bytes = entryBytes;
            }
          }
        }
        if (bytes == null) throw ApiException.badRequest("ZIP tidak berisi file .txt.");
      } else throw ApiException.badRequest("Format file harus .txt atau .zip.");
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(bytes))
          .toString();
    } catch (IOException e) {
      throw ApiException.badRequest("File tidak dapat dibaca. Gunakan UTF-8 atau ZIP yang valid.");
    }
  }
}
