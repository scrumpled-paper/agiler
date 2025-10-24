package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;
import scrumpledpaper.agiler.project.entity.Project;

public class ProjectFixture {

	public static ProjectCreateReqDto createProjectCreateReqDto() {
		return new ProjectCreateReqDto(
			"프로젝트 이름",
			"project-url_tag",
			"프로젝트 요약 설명"
		);
	}

	public static Project createProject(String url) {
		return Project.builder()
			.title("프로젝트 이름")
			.url(url)
			.summary("프로젝트 요약 설명")
			.build();
	}
}
