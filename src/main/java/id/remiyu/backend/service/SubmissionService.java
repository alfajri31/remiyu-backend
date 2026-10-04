package id.remiyu.backend.service;

import id.remiyu.backend.common.exception.ApiException;
import id.remiyu.backend.dto.response.SubmissionView;
import id.remiyu.backend.repository.SubmissionRepository;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SubmissionService {
  private final SubmissionRepository repository;
  private final TaskService tasks;

  public SubmissionService(SubmissionRepository repository, TaskService tasks) {
    this.repository = repository;
    this.tasks = tasks;
  }

  @Transactional
  public SubmissionView submit(
      UUID user, UUID workspace, UUID task, String link, String note, MultipartFile file) {
    var current = tasks.get(user, workspace, task);
    if (current.status().equals("DRAFT") || current.status().equals("DONE")) {
      throw ApiException.badRequest("Tugas harus dikonfirmasi dan belum selesai.");
    }
    link = link == null || link.isBlank() ? null : link.trim();
    if (link == null && (file == null || file.isEmpty()))
      throw ApiException.badRequest("Pilih file atau tautan hasil tugas.");
    if (note != null && note.length() > 4000)
      throw ApiException.badRequest("Catatan maksimal 4000 karakter.");
    if (link != null) {
      try {
        var uri = URI.create(link);
        if (link.length() > 2000
            || uri.getHost() == null
            || !("https".equalsIgnoreCase(uri.getScheme())
                || "http".equalsIgnoreCase(uri.getScheme()))) throw new IllegalArgumentException();
      } catch (IllegalArgumentException e) {
        throw ApiException.badRequest("Tautan tidak valid.");
      }
    }
    String filename = null;
    byte[] content = null;
    if (file != null && !file.isEmpty()) {
      filename = file.getOriginalFilename();
      if (filename == null
          || filename.length() > 255
          || filename.contains("/")
          || filename.contains("\\")
          || filename.chars().anyMatch(Character::isISOControl))
        throw ApiException.badRequest("Nama file tidak valid.");
      String extension =
          filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT);
      if (!Set.of("doc", "docx", "pdf", "jpg", "jpeg", "png", "zip", "rar").contains(extension)
          || file.getSize() > 1024 * 1024)
        throw ApiException.badRequest("Format file tidak didukung atau melebihi 1 MB.");
      try {
        content = file.getBytes();
      } catch (java.io.IOException e) {
        throw ApiException.badRequest("File tidak terbaca.");
      }
    }
    var result = repository.save(task, user, link, note, filename, content);
    tasks.status(user, workspace, task, "DONE");
    return result;
  }

  public List<SubmissionView> list(UUID user, UUID workspace, UUID task) {
    tasks.get(user, workspace, task);
    return repository.list(task);
  }
}
