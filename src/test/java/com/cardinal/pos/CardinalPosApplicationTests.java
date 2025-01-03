package com.cardinal.pos;

import com.cardinal.pos.model.Tool;
import com.cardinal.pos.repository.RentalAgreementRepository;
import com.cardinal.pos.repository.ToolRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CardinalPosApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private RentalAgreementRepository rentalAgreementRepository;

    @Test
    public void testTableCreation() {
        Tool tool = new Tool();
        tool.setToolCode("JAKR");
        tool.setToolType("Jackhammer");
        tool.setBrand("Ridgid");
        tool.setDailyRentalCharge(new BigDecimal("2.99"));
        tool.setWeekdayCharge(true);
        tool.setWeekendCharge(false);
        tool.setHolidayCharge(false);

        toolRepository.save(tool);

        assertTrue(toolRepository.findById(1L).isPresent());
    }

    @Test
    @Transactional
    public void testInsertTool() {
        Tool tool = new Tool("JAKD", "Jackhammer", "Ridgid", new BigDecimal("2.99"),
                true, false, false);
        toolRepository.save(tool);

        List<Tool> tools = toolRepository.findAll();
        assertTrue(tools.size() >= 1);
    }

    @Test
    public void testDatabaseSetup() {
        assertNotNull(toolRepository);
    }

    @Test
    public void testCheckout() throws Exception {
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKR");
        rentalParms.put("rentalDayCount", "5");
        rentalParms.put("discountPercent", "100");
        rentalParms.put("checkoutDate", "09/03/2015");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(rentalParms))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("JAKR"))
                .andExpect(jsonPath("$.rentalDayCount").value(5))
                .andExpect(jsonPath("$.discountPercent").value(100))
                .andExpect(jsonPath("$.checkoutDate").value("2015-09-03"));

    }

    @Test
    public void testCheckout_JAKR_5Days_101Discount() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKR");
        rentalParms.put("rentalDayCount", "5");
        rentalParms.put("discountPercent", "100");
        rentalParms.put("checkoutDate", "09/03/2015");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("JAKR"))
                .andExpect(jsonPath("$.rentalDayCount").value(5))
                .andExpect(jsonPath("$.discountPercent").value(100))
                .andExpect(jsonPath("$.checkoutDate").value("2015-09-03"));
    }

    @Test
    public void testCheckout_LADW_3Days_10Discount() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "LADW");
        rentalParms.put("rentalDayCount", "3");
        rentalParms.put("discountPercent", "10");
        rentalParms.put("checkoutDate", "07/02/2020");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("LADW"))
                .andExpect(jsonPath("$.rentalDayCount").value(3))
                .andExpect(jsonPath("$.discountPercent").value(10))
                .andExpect(jsonPath("$.checkoutDate").value("2020-07-02"));
    }

    @Test
    public void testCheckout_CHNS_5Days_25Discount() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "CHNS");
        rentalParms.put("rentalDayCount", "5");
        rentalParms.put("discountPercent", "25");
        rentalParms.put("checkoutDate", "07/02/2015");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("CHNS"))
                .andExpect(jsonPath("$.rentalDayCount").value(5))
                .andExpect(jsonPath("$.discountPercent").value(25))
                .andExpect(jsonPath("$.checkoutDate").value("2015-07-02"));
    }

    @Test
    public void testCheckout_JAKD_6Days_0Discount() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKD");
        rentalParms.put("rentalDayCount", "6");
        rentalParms.put("discountPercent", "0");
        rentalParms.put("checkoutDate", "09/03/2015");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("JAKD"))
                .andExpect(jsonPath("$.rentalDayCount").value(6))
                .andExpect(jsonPath("$.discountPercent").value(0))
                .andExpect(jsonPath("$.checkoutDate").value("2015-09-03"));
    }

    @Test
    public void testCheckout_JAKR_9Days_0Discount() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKR");
        rentalParms.put("rentalDayCount", "9");
        rentalParms.put("discountPercent", "0");
        rentalParms.put("checkoutDate", "07/02/2015");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("JAKR"))
                .andExpect(jsonPath("$.rentalDayCount").value(9))
                .andExpect(jsonPath("$.discountPercent").value(0))
                .andExpect(jsonPath("$.checkoutDate").value("2015-07-02"));
    }

    @Test
    public void testCheckout_JAKR_4Days_50Discount() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKR");
        rentalParms.put("rentalDayCount", "4");
        rentalParms.put("discountPercent", "50");
        rentalParms.put("checkoutDate", "07/02/2020");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool.toolCode").value("JAKR"))
                .andExpect(jsonPath("$.rentalDayCount").value(4))
                .andExpect(jsonPath("$.discountPercent").value(50))
                .andExpect(jsonPath("$.checkoutDate").value("2020-07-02"));
    }

    @Test
    public void testInvalidParms() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKR");
        rentalParms.put("rentalDayCount", "-4");
        rentalParms.put("discountPercent", "50");
        rentalParms.put("checkoutDate", "07/02/2020");

        // When
        mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void testPrintReceipt() throws Exception {
        // Given
        Map<String, String> rentalParms = new HashMap<>();
        rentalParms.put("toolCode", "JAKR");
        rentalParms.put("rentalDayCount", "5");
        rentalParms.put("discountPercent", "100");
        rentalParms.put("checkoutDate", "09/03/2015");

        // Create a rental agreement
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(rentalParms)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract UUID from response
        JSONObject jsonObject = new JSONObject(response);

        // get UUID
        String tmpId = jsonObject.getString("id");
        Long uniqueIdentifier = Long.parseLong(tmpId);

        // When
        response = mockMvc.perform(MockMvcRequestBuilders.get("/api/printReceipt/" + uniqueIdentifier))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/printReceipt/" + uniqueIdentifier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uniqueIdentifier.toString()));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
