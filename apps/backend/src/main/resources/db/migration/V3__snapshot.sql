CREATE TABLE kanban_config_snapshot (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `kanban_config_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `status_name` VARCHAR(20) NOT NULL,
    `priority` INT NOT NULL,
    `default_status` TINYINT(1) NOT NULL,
    `backlog` TINYINT(1) NOT NULL,
    `is_done` TINYINT(1) NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`profile_id`) REFERENCES `profile`(`id`),
    FOREIGN KEY (`kanban_config_id`) REFERENCES `kanban_config`(`id`)
);

CREATE TABLE issue_snapshot (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `kanban_config_id` BIGINT NOT NULL,
    `profile_id` BIGINT,
    `title` VARCHAR(20) NOT NULL,
    `is_done` TINYINT(1) NOT NULL,
    `contents` TEXT,
    `started_at` DATETIME,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`),
    FOREIGN KEY (`kanban_config_id`) REFERENCES `kanban_config`(`id`),
    FOREIGN KEY (`profile_id`) REFERENCES `profile`(`id`)
);

CREATE TABLE issue_snapshot_date_mapping (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `issue_count` INT NOT NULL,
    `snapshot_date` DATE NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    UNIQUE KEY unique_project_date (project_id, snapshot_date),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
);
