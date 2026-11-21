
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
import com.niveditha.cloudtask.service.TaskService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // CREATE
    @Override
    public TaskResponse createTask(CreateTaskRequest request) {

        Task task = new Task();

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(
                request.getPriority() != null
                        ? request.getPriority()
                        : Priority.MEDIUM
        );
        task.setStatus(TaskStatus.TODO);
        task.setDueDate(request.getDueDate());

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask);
    }

    // GET ALL
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getAllTasks(Pageable pageable) {

        return taskRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    // GET BY ID
    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {

        Task task = getTaskOrThrow(id);

        return mapToResponse(task);
    }

    // UPDATE TASK DETAILS
    @Override
    public TaskResponse updateTask(
            Long id,
            UpdateTaskRequest request
    ) {

        Task task = getTaskOrThrow(id);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    // UPDATE STATUS
    @Override
    public TaskResponse updateTaskStatus(
            Long id,
            UpdateTaskStatusRequest request
    ) {

        Task task = getTaskOrThrow(id);

        task.setStatus(request.getStatus());

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(updatedTask);
    }

    // DELETE
    @Override
    public void deleteTask(Long id) {

        Task task = getTaskOrThrow(id);

        taskRepository.delete(task);
    }

    // FILTER BY STATUS
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByStatus(
            TaskStatus status, Pageable pageable) {

        return taskRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksByPriority(
            Priority priority, Pageable pageable) {

        return taskRepository.findByPriority(priority, pageable)
                .map(this::mapToResponse);
    }

    // COMMON METHOD
    private Task getTaskOrThrow(Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with id: " + id
                        )
                );
    }

    // ENTITY TO RESPONSE DTO
    private TaskResponse mapToResponse(Task task) {

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
