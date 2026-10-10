package id.remiyu.backend.service;

import id.remiyu.backend.dto.response.ImportResultResponse;
import id.remiyu.backend.dto.response.ImportRunView;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {
  ImportResultResponse upload(UUID user, UUID workspace, MultipartFile file);

  List<ImportRunView> list(UUID user, UUID workspace);
}
