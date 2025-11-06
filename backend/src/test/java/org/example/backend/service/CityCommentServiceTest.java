package org.example.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.model.Reply;
import org.example.backend.model.ReplyDTO;
import org.example.backend.repository.CityCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CityCommentServiceTest {

    @Test
    void allComments_shouldReturnList_whenCommentsExist() {
        List<CityComment> commentList = new ArrayList<>(List.of(
                CityComment.builder().id("1").comment("Test").cityName("Berlin").build()
        ));
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.findByCityNameIgnoreCase("Berlin")).thenReturn(commentList);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService,mockCloudinary );
        List<CityComment> newList = cityCommentService.allComments("Berlin");
        assertThat(newList.getFirst().getComment()).isEqualTo("Test");
        verify(mockRepo).findByCityNameIgnoreCase("Berlin");
    }
    @Test
    void allComments_whenNoComments_shouldThrowException() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        when(mockRepo.findByCityNameIgnoreCase("Berlin")).thenReturn(Collections.emptyList());
        CityCommentService cityCommentService = new CityCommentService(mockRepo,mockNotificationService ,mockCloudinary);
        assertThrows(NoSuchElementException.class, () -> cityCommentService.allComments("Berlin"));
        verify(mockRepo).findByCityNameIgnoreCase("Berlin");
    }
    @Test
    public void testGetCommentById() {

        Optional<CityComment> cityComment = Optional.of(CityComment.builder().id("1").comment("Test").cityName("Berlin").build());
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        when(mockRepo.findById("1")).thenReturn(cityComment);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService,mockCloudinary);
        Optional<CityComment> newComment = cityCommentService.getCommentById("1");
        assertThat(newComment).isPresent();
        assertThat(newComment.get().getId()).isEqualTo("1");
        verify(mockRepo).findById("1");
    }

    @Test
    void addComment() {
        CityComment cityComment = CityComment.builder().comment("Testing").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        when(mockRepo.save(any(CityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService,mockCloudinary);
        CityComment newComment = cityCommentService.addComment(cityComment);
        assertThat(newComment.getComment()).isEqualTo("Testing");
        verify(mockRepo).save(any(CityComment.class));
    }

    @Test
    void addCommentWithImage_shouldUploadImageAndAddComment_whenFileIsProvided() throws IOException {
        CityComment comment = CityComment.builder().build();
        comment.setComment("Test Comment");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn("dummy image data".getBytes());

        Cloudinary mockCloudinary = mock(Cloudinary.class);
        Map uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://cloudinary.com/fake-image.jpg");

        Uploader mockUploader = mock(Uploader.class);
        when(mockCloudinary.uploader()).thenReturn(mockUploader);
        when(mockUploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        CityCommentService cityCommentService = spy(new CityCommentService(mockRepo, mockNotificationService, mockCloudinary));
        doReturn(comment).when(cityCommentService).addComment(comment);

        CityComment result = cityCommentService.addCommentWithImage(comment, file);

        assertThat(result.getImageUrl()).isEqualTo("https://cloudinary.com/fake-image.jpg");
        verify(mockCloudinary.uploader()).upload(any(byte[].class), anyMap());
        verify(cityCommentService).addComment(comment);
    }

    @Test
    void updateComment() throws IOException {
        CityComment cityComment = CityComment.builder().id("1").comment("Testing").build();
        CityCommentDTO cityCommentDTO = CityCommentDTO.builder().comment("Test").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        when(mockRepo.findById("1")).thenReturn(Optional.of(cityComment));
        when(mockRepo.save(any(CityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MultipartFile file = null;
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService,mockCloudinary);
        CityComment newComment = cityCommentService.updateComment("1", cityCommentDTO, file);
        assertThat(newComment.getComment()).isEqualTo("Test");
        verify(mockRepo).findById("1");
        verify(mockRepo).save(newComment);
    }

    @Test
    void updateComment_whenCommentNotFound_shouldThrowException()  {
        CityCommentDTO cityCommentDTO = CityCommentDTO.builder().comment("Test").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        when(mockRepo.findById("10")).thenReturn(Optional.empty());
        MultipartFile file = null;

        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService, mockCloudinary);
        assertThatThrownBy(() -> cityCommentService.updateComment("10", cityCommentDTO, file))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kommentar nicht gefunden: 10");

        verify(mockRepo).findById("10");
        verify(mockRepo, never()).save(any());
    }

    @Test
    void deleteCommentById() {
        String id = "1";
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        doNothing().when(mockRepo).deleteById(id);
        when(mockRepo.existsById(id)).thenReturn(true);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService,mockCloudinary);
        cityCommentService.deleteCommentById(id);
        verify(mockRepo).deleteById(id);
    }

    @Test
    void deleteCommentById_whenCommentNotFound_shouldThrowException() {
        String id = "999";
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        when(mockRepo.existsById(id)).thenReturn(false);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService,mockCloudinary);
        assertThrows(NoSuchElementException.class, () -> cityCommentService.deleteCommentById(id));
        verify(mockRepo).existsById(id);
        verify(mockRepo, never()).deleteById(any());
    }
    @Test
    void toggleLike_shouldThrowException_whenCommentNotFound() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        when(mockRepo.findById("1")).thenReturn(Optional.empty());
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockNotificationService, mockCloudinary);
        assertThatThrownBy(() -> cityCommentService.toggleLike("1", "user1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kommentar nicht gefunden");
    }

    @Test
    void toggleLike_shouldRemoveLike_whenUserAlreadyLiked() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        CityComment comment = CityComment.builder()
                .id("1")
                .username("author")
                .likesCount(1)
                .likedByUsers(new ArrayList<>(List.of("user1")))
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService =  new CityCommentService(mockRepo, mockNotificationService, mockCloudinary);
        CityComment result = cityCommentService.toggleLike("1", "user1");

        assertThat(result.getLikedByUsers()).doesNotContain("user1");
        assertThat(result.getLikesCount()).isEqualTo(0);
        verify(mockNotificationService, never()).createNotification(any(), any(), any(), any(), any(), any());
        verify(mockRepo).save(comment);
    }

    @Test
    void toggleLike_shouldAddLikeAndNotify_whenUserDidNotLikeAndIsNotAuthor() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        CityComment comment = CityComment.builder()
                .id("1")
                .username("author")
                .likesCount(0)
                .likedByUsers(new ArrayList<>())
                .cityName("Berlin")
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService =  new CityCommentService(mockRepo, mockNotificationService, mockCloudinary);
        CityComment result = cityCommentService.toggleLike("1", "user1");

        assertThat(result.getLikedByUsers()).contains("user1");
        assertThat(result.getLikesCount()).isEqualTo(1);
        verify(mockNotificationService).createNotification("author", "user1", "LIKE", "Berlin", "1", null);
        verify(mockRepo).save(comment);
    }

    @Test
    void toggleLike_shouldAddLikeWithoutNotification_whenUserIsAuthor() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        NotificationService  mockNotificationService = mock(NotificationService.class);
        CityComment comment = CityComment.builder()
                .id("1")
                .username("user1")
                .likesCount(0)
                .likedByUsers(new ArrayList<>())
                .cityName("Berlin")
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService =  new CityCommentService(mockRepo, mockNotificationService, mockCloudinary);
        CityComment result = cityCommentService.toggleLike("1", "user1");

        assertThat(result.getLikedByUsers()).contains("user1");
        assertThat(result.getLikesCount()).isEqualTo(1);
        verify(mockNotificationService, never()).createNotification(any(), any(), any(), any(), any(), any());
        verify(mockRepo).save(comment);
    }

    @Test
    void addReply_shouldThrowException_whenCommentNotFound() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        ReplyDTO replyDTO = ReplyDTO.builder()
                .username("user1")
                .text("Test reply")
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addReply("1", replyDTO))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kommentar mit ID 1 nicht gefunden.");
    }

    @Test
    void addReply_shouldAddReplyAndNotify_whenReplierIsNotAuthor() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        CityComment comment = CityComment.builder()
                .id("1")
                .username("author")
                .replies(new ArrayList<>())
                .cityName("Berlin")
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        ReplyDTO replyDTO = ReplyDTO.builder()
                .username("user1")
                .text("Test reply")
                .build();

        CityComment result = service.addReply("1", replyDTO);

        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().get(0).getText()).isEqualTo("Test reply");
        assertThat(result.getReplies().get(0).getUsername()).isEqualTo("user1");

        verify(mockNotificationService).createNotification(
                "author",
                "user1",
                "REPLY",
                "Berlin",
                "1",
                result.getReplies().get(0).getId()
        );

        verify(mockRepo).save(comment);
    }

    @Test
    void addReply_shouldAddReplyWithoutNotification_whenReplierIsAuthor() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        CityComment comment = CityComment.builder()
                .id("1")
                .username("user1")
                .replies(new ArrayList<>())
                .cityName("Berlin")
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        ReplyDTO replyDTO = ReplyDTO.builder()
                .username("user1")
                .text("Author reply")
                .build();

        CityComment result = service.addReply("1", replyDTO);

        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().get(0).getText()).isEqualTo("Author reply");
        assertThat(result.getReplies().get(0).getUsername()).isEqualTo("user1");

        verify(mockNotificationService, never()).createNotification(any(), any(), any(), any(), any(), any());

        verify(mockRepo).save(comment);
    }

    @Test
    void deleteReply_shouldThrowException_whenCommentNotFound() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        when(mockRepo.findById("1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteReply("1", "reply1", "user1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Kommentar nicht gefunden");
    }

    @Test
    void deleteReply_shouldDeleteReply_whenUserIsOwner() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        Reply reply = Reply.builder()
                .id("reply1")
                .username("user1")
                .text("Test reply")
                .build();

        CityComment comment = CityComment.builder()
                .id("1")
                .replies(new ArrayList<>(List.of(reply)))
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));
        when(mockRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        CityComment result = service.deleteReply("1", "reply1", "user1");

        assertThat(result.getReplies()).isEmpty();
        verify(mockRepo).save(comment);
    }

    @Test
    void deleteReply_shouldThrowException_whenUserIsNotOwner() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        Reply reply = Reply.builder()
                .id("reply1")
                .username("ownerUser")
                .text("Test reply")
                .build();

        CityComment comment = CityComment.builder()
                .id("1")
                .replies(new ArrayList<>(List.of(reply)))
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        assertThatThrownBy(() -> service.deleteReply("1", "reply1", "user1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Antwort wurde nicht gefunden oder du hast keine Berechtigung sie zu löschen.");

        verify(mockRepo, never()).save(any());
    }

    @Test
    void deleteReply_shouldThrowException_whenReplyDoesNotExist() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        NotificationService mockNotificationService = mock(NotificationService.class);

        CityComment comment = CityComment.builder()
                .id("1")
                .replies(new ArrayList<>())
                .build();

        when(mockRepo.findById("1")).thenReturn(Optional.of(comment));

        CityCommentService service = new CityCommentService(mockRepo, mockNotificationService, null);

        assertThatThrownBy(() -> service.deleteReply("1", "reply1", "user1"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Antwort wurde nicht gefunden oder du hast keine Berechtigung sie zu löschen.");

        verify(mockRepo, never()).save(any());
    }
}