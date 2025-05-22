package backend.academy.scrapper.entity;

import backend.academy.scrapper.linkConditions.LinkStatus;
import backend.academy.scrapper.linkConditions.LinkType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "links")
@Entity
public class Link {

    @Id
    @SequenceGenerator(name = "links_id_seq", sequenceName = "links_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "links_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "link_type")
    @Enumerated(EnumType.ORDINAL)
    private LinkType linkType;

    @Column(name = "url")
    private String url;

    @Builder.Default
    @Column(name = "checked_at")
    private OffsetDateTime checkedAt = OffsetDateTime.now();
    @Builder.Default
    @Column(name = "status")
    @Enumerated(EnumType.ORDINAL)
    private LinkStatus status = LinkStatus.ACTIVE;
    @Builder.Default
    @ManyToMany(mappedBy = "links")
    private Set<Chat> chats = new HashSet<>();

    @Override
    public String toString() {
        return "Link{" +
            "id=" + id +
            ", linkType=" + linkType +
            ", url='" + url + '\'' +
            ", checkedAt=" + checkedAt +
            ", status=" + status +
            ", chats=" + chats +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Link link)) {
            return false;
        }
        return id != null && Objects.equals(id, link.id());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}
