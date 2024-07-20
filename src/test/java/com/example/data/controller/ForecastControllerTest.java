package com.example.data.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ForecastController.class)
@AutoConfigureMockMvc
public class ForecastControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Open API 통신 테스트")
    public void callOpenApi() throws Exception {
        String baseTime = "1100";
        String baseDate = "20230525";
        String beachNum = "1";

        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();

        param.add("base_time", baseTime);
        param.add("base_date", baseDate);
        param.add("beach_num", beachNum);

        this.mvc.perform(get("/api/forecast").params(param))
                .andExpect(status().isOk())
                .andDo(print());
    }
}