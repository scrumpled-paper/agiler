CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `vendor` VARCHAR(20),
    `vendor_id` VARCHAR(50),
    `email` VARCHAR(40),
    `nickname` VARCHAR(20) NOT NULL,
    `image_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `project` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(20) NOT NULL,
    `url` VARCHAR(20),
    `tag` VARCHAR(20),
    `summary` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `project_id` BIGINT NOT NULL,
    `nickname` VARCHAR(20),
    `role` ENUM('member', 'owner') NOT NULL,
    `image_id` BIGINT NOT NULL,
    `email` TEXT,
    `description` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `kanban_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT,
    `status_name` VARCHAR(20) NOT NULL,
    `priority` INT NOT NULL,
    `default_status` TINYINT(1) NOT NULL,
    `backlog` TINYINT(1) NOT NULL,
    `is_done` TINYINT(1) NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `issue` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `kanban_config_id` BIGINT NOT NULL,
    `profile_id` BIGINT,
    `title` VARCHAR(20) NOT NULL,
    `is_done` TINYINT(1) NOT NULL,
    `contents` TEXT,
    `started_at` DATETIME,
    `due_at` DATETIME,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`kanban_config_id`) REFERENCES `kanban_config`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `image` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `url` TEXT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `label` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `name` VARCHAR(20) NOT NULL,
    `color` VARCHAR(20) NOT NULL,
    `description` VARCHAR(100),
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `issue_label` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `issue_id` BIGINT NOT NULL,
    `label_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`),
    FOREIGN KEY (`label_id`) REFERENCES `label`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `issue_status_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `issue_id` BIGINT NOT NULL,
    `profile_id` BIGINT NOT NULL,
    `from_kanban_config` BIGINT NOT NULL,
    `to_kanban_config` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`issue_id`) REFERENCES `issue`(`id`),
    FOREIGN KEY (`profile_id`) REFERENCES `profile`(`id`),
    FOREIGN KEY (`from_kanban_config`) REFERENCES `kanban_config`(`id`),
    FOREIGN KEY (`to_kanban_config`) REFERENCES `kanban_config`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `note` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `type` ENUM('retro', 'scrum', 'meeting') NOT NULL,
    `title` VARCHAR(20) NOT NULL,
    `contents` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `note_profile` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `note_id` BIGINT NOT NULL,
    `profile_id` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`note_id`) REFERENCES `note`(`id`),
    FOREIGN KEY (`profile_id`) REFERENCES `profile`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `retro_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `title` VARCHAR(20) NOT NULL,
    `description` VARCHAR(100),
    `contents` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `issue_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `title` VARCHAR(20) NOT NULL,
    `description` VARCHAR(100),
    `contents` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `scrum_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `title` VARCHAR(20) NOT NULL,
    `description` VARCHAR(100),
    `contents` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `meeting_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `title` VARCHAR(20) NOT NULL,
    `contents` TEXT,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `deleted_at` DATETIME,
    FOREIGN KEY (`project_id`) REFERENCES `project`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
