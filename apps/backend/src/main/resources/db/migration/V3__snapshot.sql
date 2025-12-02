CREATE TABLE issue_snapshot_date_mapping (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `issue_count` INT NOT NULL,
    `snapshot_date` DATE NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
);
