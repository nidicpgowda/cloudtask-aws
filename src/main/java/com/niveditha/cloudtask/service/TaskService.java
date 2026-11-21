package com.niveditha.cloudtask.service;

import com.niveditha.cloudtask.dto.request.CreateTaskRequest;
import com.niveditha.cloudtask.dto.request.UpdateTaskRequest;
import com.niveditha.cloudtask.dto.request.UpdateTaskStatusRequest;
import com.niveditha.cloudtask.dto.response.TaskResponse;
import com.niveditha.cloudtask.enums.Priority;
import com.niveditha.cloudtask.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface  TaskService {
    TaskResponse createTask(CreateTaskRequest request);

    Page<TaskResponse> getAllTasks(Pageable pageable);

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    TaskResponse updateTaskStatus(
            Long id,
            UpdateTaskStatusRequest request
    );

    void deleteTask(Long id);

    Page<TaskResponse> getTasksByStatus(TaskStatus status, Pageable pageable);

    Page<TaskResponse> getTasksByPriority(Priority priority , Pageable pageable);

}
