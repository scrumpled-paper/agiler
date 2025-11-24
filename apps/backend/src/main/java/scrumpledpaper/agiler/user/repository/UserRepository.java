package scrumpledpaper.agiler.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import scrumpledpaper.agiler.auth.oauth2.ProviderType;
import scrumpledpaper.agiler.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByVendorAndVendorId(ProviderType vendor, String vendorId);
}
