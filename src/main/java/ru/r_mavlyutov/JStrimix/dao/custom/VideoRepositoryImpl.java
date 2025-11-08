package ru.r_mavlyutov.JStrimix.dao.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import ru.r_mavlyutov.JStrimix.entity.Category;
import ru.r_mavlyutov.JStrimix.entity.User;
import ru.r_mavlyutov.JStrimix.entity.Video;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VideoRepositoryImpl implements VideoRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Video> findByAuthorUsernameAndCreatedAtBetweenCriteria(
            String authorUsername, Instant from, Instant to
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Video> cq = cb.createQuery(Video.class);

        Root<Video> video = cq.from(Video.class);
        // JOIN с автором (User)
        Join<Video, User> author = video.join("author", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>(3);
        // author.username = :authorUsername
        predicates.add(cb.equal(author.get("username"), authorUsername));
        // created_at BETWEEN :from AND :to (включительно)
        if (from != null && to != null) {
            predicates.add(cb.between(video.get("createdAt"), from, to));
        } else if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(video.get("createdAt"), from));
        } else if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(video.get("createdAt"), to));
        }

        cq.select(video).where(cb.and(predicates.toArray(new Predicate[0])))
                .orderBy(cb.desc(video.get("createdAt")));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Video> findByCategoryNameCriteria(String categoryName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Video> cq = cb.createQuery(Video.class);

        Root<Video> video = cq.from(Video.class);
        // JOIN с категорией
        Join<Video, Category> category = video.join("category", JoinType.INNER);

        Predicate byName = cb.equal(category.get("name"), categoryName);

        cq.select(video).where(byName)
                .orderBy(cb.asc(video.get("title")));

        return em.createQuery(cq).getResultList();
    }
}
