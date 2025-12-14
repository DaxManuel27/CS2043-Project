package com.example.cs2043.controllers;

import com.example.cs2043.Entities.LeaveRequest;
import com.example.cs2043.Repositories.LeaveRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(LeaveRequestApiController.class)
class LeaveRequestApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveRequestRepository leaveRequestRepository;

    private LeaveRequest testLeaveRequest1;
    private LeaveRequest testLeaveRequest2;

    @BeforeEach
    void setUp() {
        testLeaveRequest1 = new LeaveRequest(LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 15));
        testLeaveRequest1.setRequestID(1);
        testLeaveRequest1.setApproved(false);
        
        testLeaveRequest2 = new LeaveRequest(LocalDate.of(2024, 2, 20), LocalDate.of(2024, 2, 25));
        testLeaveRequest2.setRequestID(2);
        testLeaveRequest2.setApproved(true);
    }

    @Test
    void getAllLeaveRequests_ShouldReturnListOfLeaveRequests() throws Exception {
        // Given
        List<LeaveRequest> leaveRequests = Arrays.asList(testLeaveRequest1, testLeaveRequest2);
        when(leaveRequestRepository.findAll()).thenReturn(leaveRequests);

        // When & Then
        mockMvc.perform(get("/leave-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].requestID").value(1))
                .andExpect(jsonPath("$[0].approved").value(false))
                .andExpect(jsonPath("$[1].requestID").value(2))
                .andExpect(jsonPath("$[1].approved").value(true));

        verify(leaveRequestRepository, times(1)).findAll();
    }

    @Test
    void getAllLeaveRequests_WhenNoRequests_ShouldReturnEmptyList() throws Exception {
        // Given
        when(leaveRequestRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/leave-requests")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(leaveRequestRepository, times(1)).findAll();
    }

    @Test
    void createLeaveRequest_ShouldReturnCreatedLeaveRequest() throws Exception {
        // Given
        Map<String, String> requestData = new HashMap<>();
        requestData.put("startDate", "2024-03-01");
        requestData.put("endDate", "2024-03-05");
        
        LeaveRequest savedRequest = new LeaveRequest(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 5));
        savedRequest.setRequestID(3);
        
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(savedRequest);

        // When & Then
        mockMvc.perform(post("/leave-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestID").value(3))
                .andExpect(jsonPath("$.startDate").value("2024-03-01"))
                .andExpect(jsonPath("$.endDate").value("2024-03-05"))
                .andExpect(jsonPath("$.totalDays").value(5));

        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void approveLeave_WhenLeaveRequestExists_ShouldReturnSuccessMessage() throws Exception {
        // Given
        testLeaveRequest1.setApproved(false);
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(testLeaveRequest1));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest1);

        // When & Then
        mockMvc.perform(post("/leave-requests/1/approve")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request approved"));

        verify(leaveRequestRepository, times(1)).findById(1);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    

    @Test
    void rejectLeave_WhenLeaveRequestExists_ShouldReturnSuccessMessage() throws Exception {
        // Given
        testLeaveRequest1.setApproved(true);
        when(leaveRequestRepository.findById(1)).thenReturn(Optional.of(testLeaveRequest1));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest1);

        // When & Then
        mockMvc.perform(post("/leave-requests/1/reject")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request rejected"));

        verify(leaveRequestRepository, times(1)).findById(1);
        verify(leaveRequestRepository, times(1)).save(any(LeaveRequest.class));
    }

    @Test
    void rejectLeave_WhenLeaveRequestDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(leaveRequestRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/leave-requests/999/reject")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(leaveRequestRepository, times(1)).findById(999);
        verify(leaveRequestRepository, never()).save(any(LeaveRequest.class));
    }

    @Test
    void deleteLeaveRequest_WhenLeaveRequestExists_ShouldReturnOk() throws Exception {
        // Given
        when(leaveRequestRepository.existsById(1)).thenReturn(true);
        doNothing().when(leaveRequestRepository).deleteById(1);

        // When & Then
        mockMvc.perform(delete("/leave-requests/1"))
                .andExpect(status().isOk());

        verify(leaveRequestRepository, times(1)).existsById(1);
        verify(leaveRequestRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteLeaveRequest_WhenLeaveRequestDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(leaveRequestRepository.existsById(999)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/leave-requests/999"))
                .andExpect(status().isNotFound());

        verify(leaveRequestRepository, times(1)).existsById(999);
        verify(leaveRequestRepository, never()).deleteById(anyInt());
    }
}

