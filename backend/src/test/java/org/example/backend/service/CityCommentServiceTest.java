package org.example.backend.service;

import org.example.backend.model.CityComment;
import org.example.backend.model.CityCommentDTO;
import org.example.backend.repository.CityCommentRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class CityCommentServiceTest {

    @Test
    void allComments() {
        List<CityComment> commentList = new ArrayList<>(List.of(
                CityComment.builder().id("1").comment("Test").cityName("Berlin").build()
        ));
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        when(mockRepo.findByCityNameIgnoreCase("Berlin")).thenReturn(commentList);
        CityCommentService cityCommentService = new CityCommentService(mockRepo);
        List<CityComment> newList = cityCommentService.allComments("Berlin");
        assertThat(newList.getFirst().getComment()).isEqualTo("Test");
        verify(mockRepo).findByCityNameIgnoreCase("Berlin");
    }
    @Test
    public void testGetCommentById() {

        Optional<CityComment> cityComment = Optional.of(CityComment.builder().id("1").comment("Test").cityName("Berlin").build());
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        when(mockRepo.findById("1")).thenReturn(cityComment);
        CityCommentService cityCommentService = new CityCommentService(mockRepo);
        Optional<CityComment> newComment = cityCommentService.getCommentById("1");
        assertThat(newComment.get().getId()).isEqualTo("1");
        verify(mockRepo).findById("1");
    }

    @Test
    void addComment() {
        CityComment cityComment = CityComment.builder().comment("Testing").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        when(mockRepo.save(any(CityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService = new CityCommentService(mockRepo);
        CityComment newComment = cityCommentService.addComment(cityComment);
        assertThat(newComment.getComment()).isEqualTo("Testing");
        verify(mockRepo).save(any(CityComment.class));
    }

    @Test
    void updateComment() {
        CityComment cityComment = CityComment.builder().id("1").comment("Testing").build();
        CityCommentDTO cityCommentDTO = CityCommentDTO.builder().comment("Test").build();
        CityCommentRepository mockRepo = mock(CityCommentRepository.class);
        when(mockRepo.findById("1")).thenReturn(Optional.of(cityComment));
        when(mockRepo.save(any(CityComment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CityCommentService cityCommentService = new CityCommentService(mockRepo);
        CityComment newComment = cityCommentService.updateComment("1", cityCommentDTO);
        assertThat(newComment.getComment()).isEqualTo("Test");
        verify(mockRepo).findById("1");
        verify(mockRepo).save(newComment);
    }
}