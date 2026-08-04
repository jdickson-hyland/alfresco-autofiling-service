/*
 * Copyright 2026 Hyland Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.autofiling.job;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz job that delegates autofiling work to {@link AutofilingJobWorker}.
 * Uses {@link JobLockService} to ensure only one cluster node runs the job at a time.
 */
public class AutofilingJob implements Job {

    private static final Log LOG = LogFactory.getLog(AutofilingJob.class);

    private static final QName LOCK_QNAME = QName.createQName(
        "http://www.alfresco.org/model/autofiling/1.0", "AutofilingJob");
    private static final long LOCK_TTL_MS = 60000L;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        AutofilingJobWorker worker = (AutofilingJobWorker) data.get("worker");
        JobLockService jobLockService = (JobLockService) data.get("jobLockService");

        String lockToken = null;
        try {
            lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL_MS, 0, 1);
        } catch (LockAcquisitionException e) {
            LOG.debug("Autofiling job skipped — another cluster node holds the lock");
            return;
        }

        if (lockToken == null) {
            LOG.debug("Autofiling job skipped — could not acquire lock");
            return;
        }

        LOG.info("Autofiling job acquired lock — starting execution");
        try {
            worker.execute();
            LOG.info("Autofiling job execution complete");
        } finally {
            try {
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
            } catch (Exception e) {
                LOG.warn("Failed to release autofiling job lock", e);
            }
        }
    }
}
