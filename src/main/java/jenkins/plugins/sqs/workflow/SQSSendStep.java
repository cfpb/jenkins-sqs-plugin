package jenkins.plugins.sqs.workflow;

import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.sqs.*;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Workflow step to send a Slack channel notification.
 */
public class SQSSendStep extends AbstractStepImpl {

    private final @Nonnull String message;
    private String color;
    private String channel;
    private String endpoint;
    private String secretKeyId;
    private String secretKey;
    private boolean failOnError;


    @Nonnull
    public String getMessage() {
        return message;
    }

    public String getColor() {
        return color;
    }

    @DataBoundSetter
    public void setColor(String color) {
        this.color = Util.fixEmpty(color);
    }


    public String getChannel() {
        return channel;
    }

    @DataBoundSetter
    public void setChannel(String channel) {
        this.channel = Util.fixEmpty(channel);
    }

    public String getEndpoint() {
        return endpoint;
    }

    @DataBoundSetter
    public void setEndpoint(String endpoint) {
        this.endpoint = Util.fixEmpty(endpoint);
    }

    public String getSecretKeyId() {
        return secretKeyId;
    }

    @DataBoundSetter
    public void setSecretKeyId(String secretKeyId) {
        this.secretKeyId = Util.fixEmpty(secretKeyId);
    }

    public String getSecretKey() {
        return secretKey;
    }

    @DataBoundSetter
    public void setSecretKey(String secretKey) {
        this.secretKey = Util.fixEmpty(secretKey);
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    @DataBoundSetter
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @DataBoundConstructor
    public SQSSendStep(@Nonnull String message) {
        this.message = message;
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SlackSendStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "sqsSend";
        }

        @Override
        public String getDisplayName() {
            return "Send SQS message";
        }
    }

    public static class SlackSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject
        transient SQSSendStep step;

        @StepContextParameter
        transient TaskListener listener;

        @Override
        protected Void run() throws Exception {

            //default to global config values if not set in step, but allow step to override all global settings
            Jenkins jenkins;
            //Jenkins.getInstance() may return null, no message sent in that case
            try {
                jenkins = Jenkins.getInstance();
            } catch (NullPointerException ne) {
                listener.error(String.format("SQS notification failed with exception: %s", ne), ne);
                return null;
            }
            SQSNotifier.DescriptorImpl slackDesc = jenkins.getDescriptorByType(SQSNotifier.DescriptorImpl.class);
            String team = step.endpoint != null ? step.endpoint : slackDesc.getEndpoint();
            String channel = step.channel != null ? step.channel : slackDesc.getRoom();
            String secretKeyId = step.secretKeyId != null ? step.secretKeyId : slackDesc.getSecretKeyId();
            String secretKey = step.secretKey != null ? step.secretKey : slackDesc.getSecretKey();
            String color = step.color != null ? step.color : "";

            //placing in console log to simplify testing of retrieving values from global config or from step field; also used for tests
            listener.getLogger().printf("SQS Send Pipeline step configured values from global config - connector: %s, channel: %s, color: %s", step.endpoint == null, step.channel == null, step.color == null);

            SQSService slackService = getSQSService(team, channel, secretKeyId, secretKey);
            boolean publishSuccess = slackService.publish(step.message, color);
            if (!publishSuccess && step.failOnError) {
                throw new AbortException("SQS notification failed. See Jenkins logs for details.");
            } else if (!publishSuccess) {
                listener.error("Slack notification failed. See Jenkins logs for details.");
            }
            return null;
        }

        //streamline unit testing
        SQSService getSQSService(String team, String channel, String secretKeyId, String secretKey) {
            return new StandardSQSService(team, channel, secretKeyId, secretKey);
        }

    }

}
