package org.example.backend.service;

import com.cloudinary.Cloudinary;
import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.repository.CityCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CityCommentServiceTest {

    @Test
    void allComments() {
        List<CityComment> commentList = new ArrayList<>(List.of(
                CityComment.builder().id("1").comment("Test").cityName("Berlin").build()
        ));
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.findByCityNameIgnoreCase("Berlin")).thenReturn(commentList);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        List<CityComment> newList = cityCommentService.allComments("Berlin");
        assertThat(newList.getFirst().getComment()).isEqualTo("Test");
        verify(mockRepo).findByCityNameIgnoreCase("Berlin");
    }
    @Test
    void allComments_whenNoComments_shouldThrowException() {
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.findByCityNameIgnoreCase("Berlin")).thenReturn(Collections.emptyList());
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        assertThrows(NoSuchElementException.class, () -> cityCommentService.allComments("Berlin"));
        verify(mockRepo).findByCityNameIgnoreCase("Berlin");
    }
    @Test
    public void testGetCommentById() {

        Optional<CityComment> cityComment = Optional.of(CityComment.builder().id("1").comment("Test").cityName("Berlin").build());
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.findById("1")).thenReturn(cityComment);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
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
        when(mockRepo.save(any(CityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        CityComment newComment = cityCommentService.addComment(cityComment);
        assertThat(newComment.getComment()).isEqualTo("Testing");
        verify(mockRepo).save(any(CityComment.class));
    }

    @Test
    void updateComment() throws IOException {
        CityComment cityComment = CityComment.builder().id("1").comment("Testing").build();
        CityCommentDTO cityCommentDTO = CityCommentDTO.builder().comment("Test").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.findById("1")).thenReturn(Optional.of(cityComment));
        when(mockRepo.save(any(CityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MultipartFile file = null;
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        CityComment newComment = cityCommentService.updateComment("1", cityCommentDTO, file);
        assertThat(newComment.getComment()).isEqualTo("Test");
        verify(mockRepo).findById("1");
        verify(mockRepo).save(newComment);
    }

    @Test
    void updateComment_whenCommentNotFound_shouldReturnNull() throws IOException {
        CityCommentDTO cityCommentDTO = CityCommentDTO.builder().comment("Test").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.findById("10")).thenReturn(Optional.empty());
        MultipartFile file = null;
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        CityComment result = cityCommentService.updateComment("10", cityCommentDTO, file);
        assertThat(result).isNull();
        verify(mockRepo).findById("10");
        verify(mockRepo, never()).save(any());
    }

    @Test
    void deleteCommentById() {
        String id = "1";
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);

        doNothing().when(mockRepo).deleteById(id);
        when(mockRepo.existsById(id)).thenReturn(true);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        cityCommentService.deleteCommentById(id);
        verify(mockRepo).deleteById(id);
    }

    @Test
    void deleteCommentById_whenCommentNotFound_shouldThrowException() {
        String id = "999";
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        Cloudinary mockCloudinary = mock(Cloudinary.class);
        when(mockRepo.existsById(id)).thenReturn(false);
        CityCommentService cityCommentService = new CityCommentService(mockRepo, mockCloudinary);
        assertThrows(NoSuchElementException.class, () -> cityCommentService.deleteCommentById(id));
        verify(mockRepo).existsById(id);
        verify(mockRepo, never()).deleteById(any());
    }
}