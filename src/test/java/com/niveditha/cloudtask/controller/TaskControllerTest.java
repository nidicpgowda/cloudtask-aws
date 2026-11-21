package com.niveditha.cloudtask.controller;

import com.niveditha.cloudtask.dto.request.CreateTaskRequest;
import com.niveditha.cloudtask.dto.request.UpdateTaskRequest;
import com.niveditha.cloudtask.dto.request.UpdateTaskStatusRequest;
import com.niveditha.cloudtask.dto.response.TaskResponse;
import com.niveditha.cloudtask.enums.Priority;
import com.niveditha.cloudtask.enums.TaskStatus;
import com.niveditha.cloudtask.service.TaskService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private TaskResponse createSampleResponse() {
        LocalDateTime now = LocalDateTime.now();

        return new TaskResponse(
                1L,
                "Sample Task",
                "Sample description",
                Priority.MEDIUM,
                TaskStatus.TODO,
                now.plusDays(2),
                now,
                now
        );
    }

    // CREATE TASK

    @Test
    void createTask_ShouldReturnCreated() throws Exception {

        when(taskService.createTask(any(CreateTaskRequest.class)))
                .thenReturn(createSampleResponse());

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Sample Task",
                                  "description": "Sample description",
                                  "priority": "MEDIUM",
                                  "dueDate": "2027-12-25T18:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample Task"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService).createTask(any(CreateTaskRequest.class));
    }

    // GET TASK BY ID

    @Test
    void getTaskById_ShouldReturnTask() throws Exception {

        when(taskService.getTaskById(1L))
                .thenReturn(createSampleResponse());

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Sample Task"));

        verify(taskService).getTaskById(1L);
    }

    // GET ALL TASKS

    @Test
    void getAllTasks_ShouldReturnPaginatedTasks() throws Exception {

        Page<TaskResponse> taskPage =
                new PageImpl<>(List.of(createSampleResponse()));

        when(taskService.getAllTasks(any(Pageable.class)))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Sample Task"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // UPDATE TASK

    @Test
    void updateTask_ShouldReturnUpdatedTask() throws Exception {

        when(taskService.updateTask(eq(1L), any(UpdateTaskRequest.class)))
                .thenReturn(createSampleResponse());

        mockMvc.perform(put("/api/v1/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Task",
                                  "description": "Updated description",
                                  "priority": "HIGH",
                                  "dueDate": "2027-12-25T18:00:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(taskService).updateTask(eq(1L), any(UpdateTaskRequest.class));
    }

    // UPDATE STATUS

    @Test
    void updateTaskStatus_ShouldReturnUpdatedStatus() throws Exception {

        when(taskService.updateTaskStatus(
                eq(1L), any(UpdateTaskStatusRequest.class)))
                .thenReturn(createSampleResponse());

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"));

        verify(taskService).updateTaskStatus(
                eq(1L), any(UpdateTaskStatusRequest.class));
    }

    // DELETE TASK

    @Test
    void deleteTask_ShouldReturnNoContent() throws Exception {

        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/v1/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    // FILTER BY STATUS

    @Test
    void getTasksByStatus_ShouldReturnMatchingTasks() throws Exception {

        Page<TaskResponse> taskPage =
                new PageImpl<>(List.of(createSampleResponse()));

        when(taskService.getTasksByStatus(
                eq(TaskStatus.TODO), any(Pageable.class)))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/v1/tasks/status/TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Sample Task"));
    }

    // FILTER BY PRIORITY

    @Test
    void getTasksByPriority_ShouldReturnMatchingTasks() throws Exception {

        Page<TaskResponse> taskPage =
                new PageImpl<>(List.of(createSampleResponse()));

        when(taskService.getTasksByPriority(
                eq(Priority.HIGH), any(Pageable.class)))
                .thenReturn(taskPage);

        mockMvc.perform(get("/api/v1/tasks/priority/HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Sample Task"));
    }
}
