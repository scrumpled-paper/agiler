package scrumpledpaper.agiler.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
