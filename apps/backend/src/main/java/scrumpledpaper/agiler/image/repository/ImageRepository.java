package scrumpledpaper.agiler.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.image.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
