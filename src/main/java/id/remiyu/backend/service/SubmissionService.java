package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.SubmissionView;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface SubmissionService {
  SubmissionView submit(
      UUID user, UUID channel, UUID task, String link, String note, MultipartFile file);

  List<SubmissionView> list(UUID user, UUID channel, UUID task);
}
