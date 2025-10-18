package com.montes.url_shortener.infrastructure.url;

import com.montes.url_shortener.domain.url.ShortUrl;
import com.montes.url_shortener.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByCode(String code);
    List<ShortUrl> findByUser(User user);
    List<ShortUrl> findByUserOrderByUsageCountDesc(User user);
}
