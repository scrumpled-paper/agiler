CREATE TABLE `notification_subscription`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT   NOT NULL,
    `profile_id`  BIGINT   NOT NULL,
    `issue_id`    BIGINT   NOT NULL ,
    `from_kanban_config` BIGINT NOT NULL ,
    `to_kanban_config`   BIGINT NOT NULL,
    `created_at`  DATETIME NOT NULL,
    `updated_at`  DATETIME NOT NULL,
    `deleted_at`  DATETIME,
    FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`issue_id`) REFERENCES `issue` (`id`),
    FOREIGN KEY (`from_kanban_config`) REFERENCES `kanban_config` (`id`),
    FOREIGN KEY (`to_kanban_config`) REFERENCES `kanban_config` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `scheduled_notification`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `profile_id`        BIGINT       NOT NULL,
    `issue_id`          BIGINT       NOT NULL,
    `notification_time` DATETIME     NOT NULL,
    `created_at`        DATETIME     NOT NULL,
    `updated_at`        DATETIME     NOT NULL,
    `deleted_at`        DATETIME,
    `message`           VARCHAR(255) NOT NULL,
    `is_sent`           BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`),
    FOREIGN KEY (`issue_id`) REFERENCES `issue` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE profile_notification_channel
(
    `id`           BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL,
    `profile_id`   BIGINT       NOT NULL,
    `channel_type` VARCHAR(50)  NOT NULL,
    `webhook_url`  VARCHAR(255),
    `created_at`   DATETIME     NOT NULL,
    `updated_at`   DATETIME     NOT NULL,
    `deleted_at`   DATETIME,
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
