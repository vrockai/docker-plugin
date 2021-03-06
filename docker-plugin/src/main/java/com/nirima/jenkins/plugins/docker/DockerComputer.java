package com.nirima.jenkins.plugins.docker;

import com.google.common.base.Objects;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.slaves.AbstractCloudComputer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents remote (running) container
 *
 * @author magnayn
 */
public class DockerComputer extends AbstractCloudComputer<DockerSlave> {
    private static final Logger LOGGER = Logger.getLogger(DockerComputer.class.getName());

    /**
     * remember associated container id
     */
    private String containerId;

    private String cloudId;

    public DockerComputer(DockerSlave dockerSlave) {
        super(dockerSlave);
        setContainerId(dockerSlave.getContainerId());
        setCloudId(dockerSlave.getCloudId());
    }

    public DockerCloud getCloud() {
        return getNode().getCloud();
    }

    @Override
    public void taskAccepted(Executor executor, Queue.Task task) {
        super.taskAccepted(executor, task);
        LOGGER.log(Level.FINE, " Computer {0} taskAccepted", this);
    }

    @Override
    public void taskCompleted(Executor executor, Queue.Task task, long durationMS) {
        Queue.Executable executable = executor.getCurrentExecutable();

        LOGGER.log(Level.FINE, " Computer {0} taskCompleted", this);

        if (executable instanceof Run) {
            Run build = (Run) executable;
            DockerSlave slave = getNode();

            if (slave == null) {
                LOGGER.log(Level.FINE, " Ignoring TaskCompleted for {0} as node has already been removed.", this);
            } else {
                slave.setRun(build);
            }
        }

        // May take the slave offline and remove it, in which case getNode()
        // above would return null and we'd not find our DockerSlave anymore.
        super.taskCompleted(executor, task, durationMS);
    }

    @Override
    public void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {
        super.taskCompletedWithProblems(executor, task, durationMS, problems);
        LOGGER.log(Level.FINE, " Computer {0} taskCompletedWithProblems", this);
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
        getNode().setContainerId(containerId); // set for clean-ups
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
        getNode().setCloudId(cloudId);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", super.getName())
                .add("slave", getNode())
                .toString();
    }
}
