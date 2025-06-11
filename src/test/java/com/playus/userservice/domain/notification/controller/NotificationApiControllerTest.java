package com.playus.userservice.domain.notification.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.playus.userservice.ControllerTestSupport;
import com.playus.userservice.domain.user.feign.response.CommentNotificationEvent;
import com.playus.userservice.domain.user.feign.response.PartyNotificationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;


@WebMvcTest(NotificationApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationApiControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("커뮤니티 댓글 알림 생성 요청 성공")
    void createCommentNotification_ShouldReturnCreated() throws Exception {
        // given
        CommentNotificationEvent event = CommentNotificationEvent.of(
                100L,  // commentId
                200L,  // postId
                1L,    // writerId,
                1L,   // receiverId
                "새 댓글이 등록되었습니다.", // content
                true   // activated
        );

        // when & then
        mockMvc.perform(post("/user/api/notifications/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());

        then(notificationService).should()
                .sendCommentNotification(any(CommentNotificationEvent.class));
    }

    @Test
    @DisplayName("직관팟 파티 알림 생성 요청 성공")
    void createPartyNotification_ShouldReturnCreated() throws Exception {
        // given
        PartyNotificationEvent event = PartyNotificationEvent.request(
                300L,                  // partyId
                "직관팟 제목",            // partyTitle
                2L,                    // receiverId
                3L,                    // applicantId (actorId)
                "WAIT",              // requestStatus
                "참여 요청합니다."      // requireMessage
        );

        // when & then
        mockMvc.perform(post("/user/api/notifications/party")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated());

        then(notificationService).should()
                .createPartyNotification(any(PartyNotificationEvent.class));
    }

}
