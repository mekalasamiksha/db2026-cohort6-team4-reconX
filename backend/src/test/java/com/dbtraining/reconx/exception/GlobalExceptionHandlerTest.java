package com.dbtraining.reconx.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenTradeNotFound_thenProblemDetailIsReturned() throws Exception {
        mockMvc.perform(get("/test/trade-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/trade-not-found"))
                .andExpect(jsonPath("$.title").value("Trade not found"))
                .andExpect(jsonPath("$.detail").value("Trade not found: TRD-999"));
    }

    @Test
    void whenInvalidTradeException_thenProblemDetailIsReturned() throws Exception {
        mockMvc.perform(get("/test/validation-failed"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("/invalid-trade"))
                .andExpect(jsonPath("$.title").value("Invalid trade"))
                .andExpect(jsonPath("$.detail").value("quantity must be positive"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/trade-not-found")
        public void tradeNotFound() {
            throw new TradeNotFoundException("TRD-999");
        }

        @GetMapping("/test/validation-failed")
        public void validationFailed() {
            throw new InvalidTradeException("quantity must be positive");
        }
    }
}
