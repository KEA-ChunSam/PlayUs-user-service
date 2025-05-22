package com.playus.userservice.domain.notification.repository;

import com.playus.userservice.domain.user.entity.Comment;
import com.playus.userservice.domain.user.entity.Notification;
import com.playus.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByReceiver(User receiver);

	void deleteAllByReceiver(User receiver);

	void deleteAllByComment(Comment comment);

	List<Notification> findTop3ByReceiverOrderByCreatedAtDesc(User receiver);
}
