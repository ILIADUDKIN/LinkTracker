package backend.academy.scrapper.reps.jpa;

import backend.academy.scrapper.config.DatabaseAccessConfig;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.linkConditions.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@ConditionalOnBean(DatabaseAccessConfig.JPAAccessTypeConfig.class)
@Repository
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    @EntityGraph(attributePaths = {"chats"})
    Optional<Link> findByUrl(String url);

    @Query("SELECT l FROM Link l "
        + "JOIN FETCH l.chats c "
        + "WHERE l.status = :status "
        + "AND l.checkedAt < :checkedAt "
        + "AND SIZE(c) > 0")
    List<Link> findAllWithStatusAndOlderThan(
        @Param("status") LinkStatus status,
        @Param("checkedAt") OffsetDateTime checkedAt,
        Pageable pageable
    );
}
