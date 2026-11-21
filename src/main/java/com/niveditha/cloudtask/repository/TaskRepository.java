package com.niveditha.cloudtask.repository;

import com.niveditha.cloudtask.entity.Task;
import com.niveditha.cloudtask.enums.Priority;
import com.niveditha.cloudtask.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(Priority priority, Pageable pageable);


}
