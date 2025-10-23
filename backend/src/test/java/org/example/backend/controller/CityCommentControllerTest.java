package org.example.backend.controller;

import org.example.backend.model.CityComment;
import org.example.backend.repository.CityCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
class CityCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CityCommentRepository cityCommentRepository;

    @Test
    @WithMockUser
    void getCommentsByCity() throws Exception {
        cityCommentRepository.deleteAll();
        CityComment cityComment = CityComment.builder().id("1")
                .comment("Test")
                .cityName("Berlin")
                .build();
        cityCommentRepository.save(cityComment);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/comment/{cityName}", "Berlin"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        """
                                 [
                                   {
                                     "id" : "1",
                                    "comment" : "Test",
                                    "cityName" : "Berlin"
                                   }
                                   ]
                                 """
                ));
    }

    @Test
    @WithMockUser
    void getCommentById() throws Exception {

        CityComment cityComment = CityComment.builder().id("1")
                .comment("Test")
                .cityName("Berlin")
                .build();
        cityCommentRepository.save(cityComment);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/comment/getId/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        """
                                 
                                  {
                                     "id" : "1",
                                    "comment" : "Test",
                                    "cityName" : "Berlin"
                                   }
                                  
                                  """
                ));
    }

    @Test
    @WithMockUser
    void getCommentBy_whenIdDoesNotExist_shouldReturnDetailedError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/comment/getId/{id}", "4"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Kommentar mit ID: 4 nicht verfügbar"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void addComment() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                """
                {
                  "cityName": "Berlin",
                  "username": "youmna",
                  "comment": "Test"
                }
                """.getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/addcomment")
                        .file(data)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.cityName").value("Berlin"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("youmna"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment").value("Test"));
    }

    @Test
    @WithMockUser
    void addComment_withInvalidData_shouldReturn400() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                "application/json",
                """
                {
                  "cityName": "Berlin",
                  "username": "youmna",
                  "comment": ""
                }
                """.getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/addcomment")
                        .file(data)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Kommentar darf nicht leer sein"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @WithMockUser
    void updateComment() throws Exception {
        CityComment cityComment = CityComment.builder().id("1")
                .comment("Test")
                .cityName("Berlin")
                .build();
        cityCommentRepository.save(cityComment);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/comment/{id}" , "1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                  {
                                    "cityName" : "Berlin",
                                    "comment" : "Testing"
                                   }
                                 """)
                ).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(
                        """
                                 
                                   {
                                     "id" : "1",
                                    "cityName" : "Berlin",
                                    "comment" : "Testing"
                                   }
                                   
                                   """
                ));
    }

    @Test
    @WithMockUser
    void updateComment_whenIdDoesNotExist_shouldReturnError() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/comment/{id}", "5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                              {
                                "cityName" : "Berlin",
                                "comment" : "Updated Comment"
                              }
                            """)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteComment() throws Exception {

        CityComment cityComment = CityComment.builder().id("1")
                .comment("Test")
                .cityName("Berlin")
                .build();
        cityCommentRepository.save(cityComment);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/comment/{id}", "1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}