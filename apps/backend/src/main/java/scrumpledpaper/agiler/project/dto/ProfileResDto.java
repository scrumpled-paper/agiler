package scrumpledpaper.agiler.project.dto;

public record ProfileResDto (
	long memberId,
	String nickname,
	String email,
	String imageUrl,
	String role,
	String description
) {}
