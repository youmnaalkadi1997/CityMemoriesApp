package org.example.backend.controller;

import org.example.backend.model.CityComment;
import org.example.backend.repository.CityCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


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
        CityComment cityComment = CityComment.builder().id("1")
                .comment("Test")
                .cityName("Berlin")
                .build();
        cityCommentRepository.save(cityComment);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/city/{cityName}", "Berlin"))
                .andExpect(MockMvcResultMatchers.status().isOk())
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
}