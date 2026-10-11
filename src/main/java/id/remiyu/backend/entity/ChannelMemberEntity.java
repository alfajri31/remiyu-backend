package id.remiyu.backend.entity;

import id.remiyu.backend.model.enums.ChannelRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "channels_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMemberEntity {
  @EmbeddedId private ChannelMemberId id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChannelRole role;
}
