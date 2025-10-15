package nus.edu.u.task.service;

import java.util.List;
import nus.edu.u.task.domain.vo.task.TaskCreateReqVO;
import nus.edu.u.task.domain.vo.task.TaskDashboardRespVO;
import nus.edu.u.task.domain.vo.task.TaskRespVO;
import nus.edu.u.task.domain.vo.task.TaskUpdateReqVO;

public interface TaskApplicationService {
    TaskRespVO createTask(Long eventId, TaskCreateReqVO reqVO);

    TaskRespVO updateTask(Long eventId, Long taskId, TaskUpdateReqVO reqVO, Integer type);

    void deleteTask(Long eventId, Long taskId);

    TaskRespVO getTask(Long eventId, Long taskId);

    List<TaskRespVO> listTasksByEvent(Long eventId);

    List<TaskRespVO> listTasksByMember(Long memberId);

    TaskDashboardRespVO getByMemberId(Long memberId);
}
