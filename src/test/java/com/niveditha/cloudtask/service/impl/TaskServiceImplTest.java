
package com.niveditha.cloudtask.service.impl;

import com.niveditha.cloudtask.dto.request.CreateTaskRequest;
import com.niveditha.cloudtask.dto.request.UpdateTaskRequest;
import com.niveditha.cloudtask.dto.request.UpdateTaskStatusRequest;
import com.niveditha.cloudtask.dto.response.TaskResponse;
import com.niveditha.cloudtask.entity.Task;
import com.niveditha.cloudtask.enums.Priority;
import com.niveditha.cloudtask.enums.TaskStatus;
import com.niveditha.cloudtask.exception.ResourceNotFoundException;
import com.niveditha.cloudtask.repository.TaskRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    // CREATE TASK

    @Test
    void createTask_ShouldCreateTaskWithDefaultValues() {

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Complete CloudTask");
        request.setDescription("Complete backend implementation");

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(request);

        assertNotNull(response);
        assertEquals("Complete CloudTask", response.getTitle());
        assertEquals("Complete backend implementation", response.getDescription());
        assertEquals(Priority.MEDIUM, response.getPriority());
        assertEquals(TaskStatus.TODO, response.getStatus());

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_ShouldUseProvidedPriority() {

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Prepare deployment");
        request.setPriority(Priority.HIGH);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(request);

        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(TaskStatus.TODO, response.getStatus());
    }

    // GET TASK BY ID

    @Test
    void getTaskById_ShouldReturnTaskWhenFound() {

        Task task = createSampleTask(1L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Sample Task", response.getTitle());

        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_ShouldThrowExceptionWhenNotFound() {

        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> taskService.getTaskById(99L)
                );

        assertEquals("Task not found with id: 99", exception.getMessage());
    }

    // GET ALL TASKS

    @Test
    void getAllTasks_ShouldReturnPaginatedTasks() {

        Pageable pageable = PageRequest.of(0, 10);

        List<Task> tasks = List.of(
                createSampleTask(1L),
                createSampleTask(2L)
        );

        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(pageable))
                .thenReturn(taskPage);

        Page<TaskResponse> response =
                taskService.getAllTasks(pageable);

        assertEquals(2, response.getTotalElements());
        assertEquals("Sample Task", response.getContent().get(0).getTitle());

        verify(taskRepository).findAll(pageable);
    }

    // UPDATE TASK

    @Test
    void updateTask_ShouldUpdateExistingTask() {

        Task task = createSampleTask(1L);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated description");
        request.setPriority(Priority.HIGH);

        LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
        request.setDueDate(dueDate);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response =
                taskService.updateTask(1L, request);

        assertEquals("Updated Task", response.getTitle());
        assertEquals("Updated description", response.getDescription());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(dueDate, response.getDueDate());

        verify(taskRepository).save(task);
    }

    // UPDATE STATUS

    @Test
    void updateTaskStatus_ShouldUpdateStatus() {

        Task task = createSampleTask(1L);

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest();

        request.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response =
                taskService.updateTaskStatus(1L, request);

        assertEquals(TaskStatus.COMPLETED, response.getStatus());

        verify(taskRepository).save(task);
    }

    // DELETE TASK

    @Test
    void deleteTask_ShouldDeleteExistingTask() {

        Task task = createSampleTask(1L);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    // FILTER BY STATUS

    @Test
    void getTasksByStatus_ShouldReturnMatchingTasks() {

        Pageable pageable = PageRequest.of(0, 10);

        List<Task> tasks = List.of(createSampleTask(1L));

        Page<Task> taskPage =
                new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findByStatus(TaskStatus.TODO, pageable))
                .thenReturn(taskPage);

        Page<TaskResponse> response =
                taskService.getTasksByStatus(TaskStatus.TODO, pageable);

        assertEquals(1, response.getTotalElements());

        verify(taskRepository)
                .findByStatus(TaskStatus.TODO, pageable);
    }

    // FILTER BY PRIORITY

    @Test
    void getTasksByPriority_ShouldReturnMatchingTasks() {

        Pageable pageable = PageRequest.of(0, 10);

        List<Task> tasks = List.of(createSampleTask(1L));

        Page<Task> taskPage =
                new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findByPriority(Priority.HIGH, pageable))
                .thenReturn(taskPage);

        Page<TaskResponse> response =
                taskService.getTasksByPriority(Priority.HIGH, pageable);

        assertEquals(1, response.getTotalElements());

        verify(taskRepository)
                .findByPriority(Priority.HIGH, pageable);
    }

    // TEST DATA HELPER

    private Task createSampleTask(Long id) {


        Task task = new Task();

        task.setId(id);
        task.setTitle("Sample Task");
        task.setDescription("Sample description");
        task.setPriority(Priority.MEDIUM);
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(LocalDateTime.now().plusDays(2));

        return task;
    }
}
