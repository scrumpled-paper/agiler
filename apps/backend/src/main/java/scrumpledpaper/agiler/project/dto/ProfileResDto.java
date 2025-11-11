package scrumpledpaper.agiler.project.dto;

public record ProfileResDto (
	long profileId,
	String nickname,
	String email,
	String imageUrl,
	String role,
	String description
) {}
