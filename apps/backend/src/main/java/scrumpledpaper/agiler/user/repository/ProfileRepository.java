package scrumpledpaper.agiler.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.user.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
