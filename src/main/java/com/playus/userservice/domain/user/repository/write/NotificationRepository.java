package com.playus.userservice.domain.user.repository.write;

import com.playus.userservice.domain.user.entity.Notification;
import com.playus.userservice.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByReceiver(User receiver);
	void deleteAllByReceiver(User receiver);
	void deleteAllByCommentId(Long commentId);
	List<Notification> findTop3ByReceiverOrderByCreatedAtDesc(User receiver);
}
