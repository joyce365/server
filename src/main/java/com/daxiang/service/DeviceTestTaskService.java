package com.daxiang.service;

import com.daxiang.mbg.mapper.DeviceTestTaskMapper;
import com.daxiang.mbg.po.DeviceTestTaskExample;
import com.daxiang.model.PageRequest;
import com.daxiang.model.Response;
import com.daxiang.model.vo.Testcase;
import com.github.pagehelper.PageHelper;
import com.daxiang.mbg.po.DeviceTestTask;
import com.daxiang.model.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by jiangyitao.
 */
@Slf4j
@Service
public class DeviceTestTaskService {

    @Autowired
    private DeviceTestTaskMapper deviceTestTaskMapper;

    public Response update(DeviceTestTask deviceTestTask) {
        if (deviceTestTask.getId() == null) {
            return Response.fail("deviceTestTaskId不能为空");
        }

        int insertRow = deviceTestTaskMapper.updateByPrimaryKeySelective(deviceTestTask);
        if (insertRow == 1) {
            return Response.success("更新成功");
        } else {
            return Response.fail("更新失败，请稍后重试");
        }
    }

    public Response list(DeviceTestTask deviceTestTask, PageRequest pageRequest) {
        boolean needPaging = pageRequest.needPaging();
        if (needPaging) {
            PageHelper.startPage(pageRequest.getPageNum(), pageRequest.getPageSize());
        }

        List<DeviceTestTask> deviceTestTasks = selectByDeviceTestTask(deviceTestTask);

        if (needPaging) {
            long total = Page.getTotal(deviceTestTasks);
            return Response.success(Page.build(deviceTestTasks, total));
        } else {
            return Response.success(deviceTestTasks);
        }
    }

    public List<DeviceTestTask> selectByDeviceTestTask(DeviceTestTask deviceTestTask) {
        if (deviceTestTask == null) {
            deviceTestTask = new DeviceTestTask();
        }

        DeviceTestTaskExample deviceTestTaskExample = new DeviceTestTaskExample();
        DeviceTestTaskExample.Criteria criteria = deviceTestTaskExample.createCriteria();

        if (deviceTestTask.getId() != null) {
            criteria.andIdEqualTo(deviceTestTask.getId());
        }
        if (deviceTestTask.getTestTaskId() != null) {
            criteria.andTestTaskIdEqualTo(deviceTestTask.getTestTaskId());
        }
        if (!StringUtils.isEmpty(deviceTestTask.getDeviceId())) {
            criteria.andDeviceIdEqualTo(deviceTestTask.getDeviceId());
        }
        if (deviceTestTask.getStatus() != null) {
            criteria.andStatusEqualTo(deviceTestTask.getStatus());
        }

        return deviceTestTaskMapper.selectByExampleWithBLOBs(deviceTestTaskExample);
    }

    public Response findFirstUnStartDeviceTestTask(String deviceId) {
        if (StringUtils.isEmpty(deviceId)) {
            return Response.fail("deviceId不能为空");
        }

        DeviceTestTaskExample deviceTestTaskExample = new DeviceTestTaskExample();
        DeviceTestTaskExample.Criteria criteria = deviceTestTaskExample.createCriteria();
        criteria.andDeviceIdEqualTo(deviceId).andStatusEqualTo(DeviceTestTask.UNSTART_STATUS);
        deviceTestTaskExample.setOrderByClause("id asc limit 1");

        List<DeviceTestTask> deviceTestTasks = deviceTestTaskMapper.selectByExampleWithBLOBs(deviceTestTaskExample);

        if (CollectionUtils.isEmpty(deviceTestTasks)) {
            return Response.success();
        } else {
            return Response.success(deviceTestTasks.get(0));
        }
    }

    public Response updateTestcase(Integer deviceTestTaskId, Testcase testcase) {
        if (deviceTestTaskId == null) {
            return Response.fail("deviceTestTaskId不能为空");
        }

        DeviceTestTask deviceTestTask = deviceTestTaskMapper.selectByPrimaryKey(deviceTestTaskId);
        if (deviceTestTask == null) {
            return Response.fail("DeviceTestTask不存在");
        } else {
            // 更新testcase运行结果
            List<Testcase> testcases = deviceTestTask.getTestcases();
            for (Testcase tc : testcases) {
                if (tc.getId() == testcase.getId()) {
                    if (testcase.getStatus() != null) {
                        tc.setStatus(testcase.getStatus());
                    }
                    if (testcase.getStartTime() != null) {
                        tc.setStartTime(testcase.getStartTime());
                    }
                    if (testcase.getEndTime() != null) {
                        tc.setEndTime(testcase.getEndTime());
                    }
                    if (!StringUtils.isEmpty(testcase.getFailInfo())) {
                        tc.setFailInfo(testcase.getFailInfo());
                    }
                    if (!StringUtils.isEmpty(testcase.getFailImgUrl())) {
                        tc.setFailImgUrl(testcase.getFailImgUrl());
                    }
                    if (!StringUtils.isEmpty(testcase.getVideoUrl())) {
                        tc.setVideoUrl(testcase.getVideoUrl());
                    }
                    break;
                }
            }
            int updateRow = deviceTestTaskMapper.updateByPrimaryKeySelective(deviceTestTask);
            if (updateRow == 1) {
                return Response.success("更新成功");
            } else {
                return Response.fail("更新失败");
            }
        }
    }

    public int insertSelective(DeviceTestTask deviceTestTask) {
        return deviceTestTaskMapper.insertSelective(deviceTestTask);
    }

    public List<DeviceTestTask> findByTestTaskId(Integer testTaskId) {
        DeviceTestTask deviceTestTask = new DeviceTestTask();
        deviceTestTask.setTestTaskId(testTaskId);
        return selectByDeviceTestTask(deviceTestTask);
    }
}