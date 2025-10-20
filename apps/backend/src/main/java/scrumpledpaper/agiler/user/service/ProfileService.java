package scrumpledpaper.agiler.user.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import scrumpledpaper.agiler.project.entity.Project;
import scrumpledpaper.agiler.user.entity.Profile;
import scrumpledpaper.agiler.user.entity.Role;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.mapper.ProfileMapper;
import scrumpledpaper.agiler.user.repository.ProfileRepository;

@Service
@RequiredArgsConstructor
public class ProfileService {
	private final ProfileMapper profileMapper;
	private final ProfileRepository profileRepository;

	public void createDefaultProfile(User user, Project savedProject, Role role) {
		Profile defaultProfile = profileMapper.toEntity(user, savedProject, role);
		profileRepository.save(defaultProfile);
	}
}
