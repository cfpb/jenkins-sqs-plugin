package jenkins.plugins.sqs.workflow;

import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.sqs.SQSNotifier;
import jenkins.plugins.sqs.SQSService;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Traditional Unit tests, allows testing null Jenkins,getInstance()
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, SQSSendStep.class})
public class SQSSendStepTest {

    @Mock
    TaskListener taskListenerMock;
    @Mock
    PrintStream printStreamMock;
    @Mock
    PrintWriter printWriterMock;
    @Mock
    StepContext stepContextMock;
    @Mock
    SQSService sqsServiceMock;
    @Mock
    Jenkins jenkins;
    @Mock
    SQSNotifier.DescriptorImpl sqsDescMock;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        when(jenkins.getDescriptorByType(SQSNotifier.DescriptorImpl.class)).thenReturn(sqsDescMock);
    }

    @Test
    public void testStepOverrides() throws Exception {
        SQSSendStep.SlackSendStepExecution stepExecution = spy(new SQSSendStep.SlackSendStepExecution());
        SQSSendStep sqsSendStep = new SQSSendStep("message");
        sqsSendStep.setEndpoint("endpoint");
        sqsSendStep.setChannel("channel");
        sqsSendStep.setSecretKeyId("keyid");
        sqsSendStep.setSecretKey("key");
        sqsSendStep.setColor("good");
        stepExecution.step = sqsSendStep;

        when(Jenkins.getInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSQSService(anyString(), anyString(), anyString(), anyString())).thenReturn(sqsServiceMock);
        when(sqsServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSQSService("endpoint", "channel", "keyid", "key");
        verify(sqsServiceMock, times(1)).publish("message", "good");
        assertFalse(stepExecution.step.isFailOnError());
    }

    @Test
    public void testValuesForGlobalConfig() throws Exception {

        SQSSendStep.SlackSendStepExecution stepExecution = spy(new SQSSendStep.SlackSendStepExecution());
        stepExecution.step = new SQSSendStep("message");

        when(Jenkins.getInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(sqsDescMock.getEndpoint()).thenReturn("globalEndpoint");
        when(sqsDescMock.getRoom()).thenReturn("globalChannel");
        when(sqsDescMock.getSecretKey()).thenReturn("globalKey");
        when(sqsDescMock.getSecretKeyId()).thenReturn("globalKeyId");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSQSService(anyString(), anyString(), anyString(), anyString())).thenReturn(sqsServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSQSService("globalEndpoint","globalChannel", "globalKeyId", "globalKey");
        verify(sqsServiceMock, times(1)).publish("message", "");
        assertNull(stepExecution.step.getEndpoint());
        assertNull(stepExecution.step.getChannel());
        assertNull(stepExecution.step.getSecretKeyId());
        assertNull(stepExecution.step.getSecretKey());
        assertNull(stepExecution.step.getColor());
    }

    @Test
    public void testNonNullEmptyColor() throws Exception {

        SQSSendStep.SlackSendStepExecution stepExecution = spy(new SQSSendStep.SlackSendStepExecution());
        SQSSendStep sqsSendStep = new SQSSendStep("message");
        sqsSendStep.setColor("");
        stepExecution.step = sqsSendStep;

        when(Jenkins.getInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSQSService(anyString(), anyString(), anyString(), anyString())).thenReturn(sqsServiceMock);

        stepExecution.run();
        verify(sqsServiceMock, times(1)).publish("message", "");
        assertNull(stepExecution.step.getColor());
    }

    @Test
    public void testNullJenkinsInstance() throws Exception {

        SQSSendStep.SlackSendStepExecution stepExecution = spy(new SQSSendStep.SlackSendStepExecution());
        stepExecution.step = new SQSSendStep("message");

        when(Jenkins.getInstance()).thenThrow(NullPointerException.class);

        stepExecution.listener = taskListenerMock;

        when(taskListenerMock.error(anyString())).thenReturn(printWriterMock);
        doNothing().when(printStreamMock).println();

        stepExecution.run();
        verify(taskListenerMock, times(1)).error(anyString(), any(Exception.class));
    }
}
