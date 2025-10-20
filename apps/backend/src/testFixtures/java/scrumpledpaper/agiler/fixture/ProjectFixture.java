package scrumpledpaper.agiler.fixture;

import scrumpledpaper.agiler.project.dto.ProjectCreateReqDto;

public class ProjectFixture {

	public static ProjectCreateReqDto createProjectCreateReqDto() {
		return new ProjectCreateReqDto(
			"프로젝트 이름",
			"project-url",
			"프로젝트 태그",
			"프로젝트 요약 설명"
		);
	}
}
