package com.fast.dev.server.hotupdate.model;

import com.fast.dev.server.hotupdate.type.TaskStat;

/**
 * 同步任务
 * 
 * @作者 练书锋
 * @联系 251708339@qq.com
 * @时间 2017年9月11日
 *
 */
public class UpdateTask {
	// 上次同步时间
	private String lastUpdateDate;
	// 任务状态
	private TaskStat taskStat = TaskStat.Finish;

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public TaskStat getTaskStat() {
		return taskStat;
	}

	public void setTaskStat(TaskStat taskStat) {
		this.taskStat = taskStat;
	}

}
