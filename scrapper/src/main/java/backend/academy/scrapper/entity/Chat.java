package backend.academy.scrapper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
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

@Table(name = "chats")
@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "chats_id_gen")
    @SequenceGenerator(name = "chats_id_gen"
        ,
        allocationSize = 1,
        sequenceName =
            "chats_id_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "chatId", nullable = false, unique = true)
    private Long chatId;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "chats_links",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "link_id"))
    private Set<Link> links = new HashSet<>();

    public void addLink(Link link) {
        links.add(link);
        link.chats().add(this);
    }

    public void removeLink(Link link) {
        links.remove(link);
        link.chats().remove(this);
    }

    public Optional<Link> findLinkByUrl(String url) {
        return links.stream()
            .filter(it -> it.url().equals(url))
            .findFirst();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Chat chat)) {
            return false;
        }
        return id != null && Objects.equals(id, chat.id());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Chat{" +
            "id=" + id +
            ", chatId=" + chatId +
//            ", links=" + links + //TODO recursieve call (Link -> Chat -> Link)
            '}';
    }
}
