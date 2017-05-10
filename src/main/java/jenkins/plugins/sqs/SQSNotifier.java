package jenkins.plugins.sqs;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.listeners.ItemListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static hudson.Util.fixNull;
//import javax.annotation.CheckForNull;

public class SQSNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(SQSNotifier.class.getName());

    private String endpoint;
    private String secretKeyId;
    private String secretKey;
    private String buildServerUrl;
    private String room;
    private String sendAs;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean sqsIncludeTestSummary;
    transient private boolean showCommitList;
    private CommitInfoChoice sqsCommitInfoChoice;
    private boolean includeCustomSQSMessage;
    private String customSQSMessage;

    @Override
    public DescriptorImpl getDescriptor() {
	return (DescriptorImpl) super.getDescriptor();
    }

    public String getSecretKeyId() {
	return secretKeyId;
    }

    public String getSecretKey() {
	return secretKey;
    }

    public String getEndpoint() {
	return endpoint;
    }

    public String getRoom() {
	return room;
    }

    public String getBuildServerUrl() {
	if(buildServerUrl == null || buildServerUrl.equals("")) {
	    JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
	    return jenkinsConfig.getUrl();
	}
	else {
	    return buildServerUrl;
	}
    }

    public String getSendAs() {
	return sendAs;
    }

    public boolean getStartNotification() {
	return startNotification;
    }

    public boolean getNotifySuccess() {
	return notifySuccess;
    }

    public CommitInfoChoice getSQSCommitInfoChoice() {
	return sqsCommitInfoChoice;
    }

    public boolean getNotifyAborted() {
	return notifyAborted;
    }

    public boolean getNotifyFailure() {
	return notifyFailure;
    }

    public boolean getNotifyNotBuilt() {
	return notifyNotBuilt;
    }

    public boolean getNotifyUnstable() {
	return notifyUnstable;
    }

    public boolean getNotifyBackToNormal() {
	return notifyBackToNormal;
    }

    public boolean sqsIncludeTestSummary() {
	return sqsIncludeTestSummary;
    }

    public boolean getNotifyRepeatedFailure() {
	return notifyRepeatedFailure;
    }

    public boolean includeCustomSQSMessage() {
	return includeCustomSQSMessage;
    }

    public String getCustomSQSMessage() {
	return customSQSMessage;
    }

    public void setEndpoint(@CheckForNull String endpoint) {
	this.endpoint = fixNull(endpoint);
    }

    @DataBoundSetter public void setSecretKey(@CheckForNull String secretKey) {
	this.secretKey = fixNull(secretKey);
    }

    @DataBoundSetter public void setSecretKeyId(@CheckForNull String secretKeyId) {
	this.secretKeyId = fixNull(secretKeyId);
    }

    @DataBoundSetter public void setRoom(@CheckForNull String room) {
	this.room = fixNull(room);
    }

    @DataBoundSetter public void setBuildServerUrl(@CheckForNull String buildServerUrl) {
	this.buildServerUrl = fixNull(buildServerUrl);
    }

    @DataBoundSetter public void setSendAs(@CheckForNull String sendAs) {
	this.sendAs = fixNull(sendAs);
    }

    @DataBoundSetter public void setStartNotification(boolean startNotification) {
	this.startNotification = startNotification;
    }

    @DataBoundSetter public void setNotifySuccess(boolean notifySuccess) {
	this.notifySuccess = notifySuccess;
    }

    @DataBoundSetter public void setSQSCommitInfoChoice(CommitInfoChoice sqsCommitInfoChoice) {
	this.sqsCommitInfoChoice = sqsCommitInfoChoice;
    }

    @DataBoundSetter public void setNotifyAborted(boolean notifyAborted) {
	this.notifyAborted = notifyAborted;
    }

    @DataBoundSetter public void setNotifyFailure(boolean notifyFailure) {
	this.notifyFailure = notifyFailure;
    }

    @DataBoundSetter public void setNotifyNotBuilt(boolean notifyNotBuilt) {
	this.notifyNotBuilt = notifyNotBuilt;
    }

    @DataBoundSetter public void setNotifyUnstable(boolean notifyUnstable) {
	this.notifyUnstable = notifyUnstable;
    }

    @DataBoundSetter public void setNotifyBackToNormal(boolean notifyBackToNormal) {
	this.notifyBackToNormal = notifyBackToNormal;
    }

    @DataBoundSetter public void setSQSIncludeTestSummary(boolean sqsIncludeTestSummary) {
	this.sqsIncludeTestSummary = sqsIncludeTestSummary;
    }

    @DataBoundSetter public void setNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
	this.notifyRepeatedFailure = notifyRepeatedFailure;
    }

    @DataBoundSetter public void setIncludeCustomMessage(boolean includeCustomSQSMessage) {
	this.includeCustomSQSMessage = includeCustomSQSMessage;
    }

    @DataBoundSetter public void setCustomMessage(@CheckForNull String customSQSMessage) {
	this.customSQSMessage = fixNull(customSQSMessage);
    }

    @DataBoundConstructor
    public SQSNotifier(final String endpoint) {
	super();
	this.setEndpoint(endpoint);
    }

    @Deprecated
    public SQSNotifier(final String endpoint, final String room, final String buildServerUrl,
	    final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
	    final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyBackToNormal,
	    final boolean notifyRepeatedFailure, final boolean sqsIncludeTestSummary, final CommitInfoChoice sqsCommitInfoChoice,
	    boolean includeCustomSQSMessage, String customSQSMessage, final String secretKey, final String secretKeyId) {
	super();
	this.setEndpoint(endpoint);
  this.setSecretKey(secretKey);
  this.setSecretKeyId(secretKeyId);
	this.setBuildServerUrl(buildServerUrl);
	this.setRoom(room);
	this.setSendAs(sendAs);
	this.setStartNotification(startNotification);
	this.setNotifyAborted(notifyAborted);
	this.setNotifyFailure(notifyFailure);
	this.setNotifyNotBuilt(notifyNotBuilt);
	this.setNotifySuccess(notifySuccess);
	this.setNotifyUnstable(notifyUnstable);
	this.setNotifyBackToNormal(notifyBackToNormal);
	this.setNotifyRepeatedFailure(notifyRepeatedFailure);
	this.setSQSIncludeTestSummary(sqsIncludeTestSummary);
	this.setSQSCommitInfoChoice(sqsCommitInfoChoice);
	this.setIncludeCustomMessage(includeCustomSQSMessage);
	this.setCustomMessage(customSQSMessage);
    }

    public BuildStepMonitor getRequiredMonitorService() {
	return BuildStepMonitor.NONE;
    }

    public SQSService newSQSService(AbstractBuild r, BuildListener listener) {
	String endpoint = this.endpoint;
	if (StringUtils.isEmpty(endpoint)) {
	    endpoint = getDescriptor().getEndpoint();
	}

  String secretKey = this.secretKey;
	if (StringUtils.isEmpty(secretKey)) {
	    secretKey = getDescriptor().getSecretKey();
	}

  String secretKeyId = this.secretKeyId;
	if (StringUtils.isEmpty(secretKeyId)) {
	    secretKeyId = getDescriptor().getSecretKeyId();
	}

	String room = this.room;
	if (StringUtils.isEmpty(room)) {
	    room = getDescriptor().getRoom();
	}

	EnvVars env = null;
	try {
	    env = r.getEnvironment(listener);
	} catch (Exception e) {
	    listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
	    env = new EnvVars();
	}
	endpoint = env.expand(endpoint);
	room = env.expand(room);
  secretKeyId = env.expand(secretKeyId);
  secretKey = env.expand(secretKey);

	return new StandardSQSService(endpoint, room, secretKeyId, secretKey);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
	return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
	if (startNotification) {
	    Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
	    for (Publisher publisher : map.values()) {
		if (publisher instanceof SQSNotifier) {
		    logger.info("Invoking Started...");
		    new ActiveNotifier((SQSNotifier) publisher, listener).started(build);
		}
	    }
	}
	return super.prebuild(build, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

	private String endpoint;
	private String room;
  private String secretKey;
  private String secretKeyId;
	private String buildServerUrl;
	private String sendAs;

	public static final CommitInfoChoice[] COMMIT_INFO_CHOICES = CommitInfoChoice.values();

	public DescriptorImpl() {
	    load();
	}

	public String getEndpoint() {
	    return endpoint;
	}

	public String getRoom() {
	    return room;
	}

  public String getSecretKey() {
	    return secretKey;
	}

  public String getSecretKeyId() {
	    return secretKeyId;
	}

	public String getBuildServerUrl() {
	    if(buildServerUrl == null || buildServerUrl.equals("")) {
		JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
		return jenkinsConfig.getUrl();
	    }
	    else {
		return buildServerUrl;
	    }
	}

	public String getSendAs() {
	    return sendAs;
	}

	public boolean isApplicable(Class<? extends AbstractProject> aClass) {
	    return true;
	}

	@Override
	public SQSNotifier newInstance(StaplerRequest sr, JSONObject json) {
	    if (sr == null) {
		return null;
	    }
	    String endpoint = sr.getParameter("endpoint");
	    String room = sr.getParameter("room");
      String secretKeyId = sr.getParameter("secretKeyId");
      String secretKey = sr.getParameter("secretKey");
	    boolean startNotification = "true".equals(sr.getParameter("sqsStartNotification"));
	    boolean notifySuccess = "true".equals(sr.getParameter("sqsNotifySuccess"));
	    boolean notifyAborted = "true".equals(sr.getParameter("sqsNotifyAborted"));
	    boolean notifyNotBuilt = "true".equals(sr.getParameter("sqsNotifyNotBuilt"));
	    boolean notifyUnstable = "true".equals(sr.getParameter("sqsNotifyUnstable"));
	    boolean notifyFailure = "true".equals(sr.getParameter("sqsNotifyFailure"));
	    boolean notifyBackToNormal = "true".equals(sr.getParameter("sqsNotifyBackToNormal"));
	    boolean notifyRepeatedFailure = "true".equals(sr.getParameter("sqsNotifyRepeatedFailure"));
	    boolean sqsIncludeTestSummary = "true".equals(sr.getParameter("sqsIncludeTestSummary"));
	    CommitInfoChoice sqsCommitInfoChoice = CommitInfoChoice.forDisplayName(sr.getParameter("sqsCommitInfoChoice"));
	    boolean includeCustomSQSMessage = "on".equals(sr.getParameter("includeCustomSQSMessage"));
	    String customSQSMessage = sr.getParameter("customSQSMessage");
	    return new SQSNotifier(endpoint, room, buildServerUrl, sendAs, startNotification, notifyAborted,
		    notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
		    sqsIncludeTestSummary, sqsCommitInfoChoice, includeCustomSQSMessage, customSQSMessage, secretKeyId, secretKey);
	}

	@Override
	public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
	    endpoint = sr.getParameter("endpoint");
	    room = sr.getParameter("room");
      secretKeyId = sr.getParameter("secretKeyId");
      secretKey = sr.getParameter("secretKey");
	    buildServerUrl = sr.getParameter("sqsBuildServerUrl");
	    sendAs = sr.getParameter("sqsSendAs");
	    if(buildServerUrl == null || buildServerUrl.equals("")) {
		JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
		buildServerUrl = jenkinsConfig.getUrl();
	    }
	    if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
		buildServerUrl = buildServerUrl + "/";
	    }
	    save();
	    return super.configure(sr, formData);
	}

	SQSService getSQSService(final String endpoint, final String room, final String secretKey, final String secretKeyId) {
	    return new StandardSQSService(endpoint, room, secretKey, secretKeyId);
	}

	@Override
	public String getDisplayName() {
	    return "SQS Notifications";
	}

	public FormValidation doTestConnection(@QueryParameter("sqsEndpoint") final String endpoint,
		@QueryParameter("sqsRoom") final String room,
		@QueryParameter("sqsBuildServerUrl") final String buildServerUrl) throws FormException {
	    try {
		String targetEndpoint = endpoint;
		if (StringUtils.isEmpty(targetEndpoint)) {
		    targetEndpoint = this.endpoint;
		}
		String targetRoom = room;
		if (StringUtils.isEmpty(targetRoom)) {
		    targetRoom = this.room;
		}
		String targetBuildServerUrl = buildServerUrl;
		if (StringUtils.isEmpty(targetBuildServerUrl)) {
		    targetBuildServerUrl = this.buildServerUrl;
		}
		SQSService testSQSService = getSQSService(targetEndpoint, targetRoom, null, null);
		String message = "SQS/Jenkins plugin: you're all set! (parameters: " +
		    "endpoint='" + targetEndpoint + "', " +
		    "room='" + targetRoom + "', " +
		    "buildServerUrl='" + targetBuildServerUrl + "'" +
		    ")";
		boolean success = testSQSService.publish(message, "good");
		return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
	    } catch (Exception e) {
		return FormValidation.error("Client error : " + e.getMessage());
	    }
	}
    }

    @Deprecated
    public static class SQSJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {

	private String endpoint;
	private String room;
  private String secretKeyId;
  private String secretKey;
	private boolean startNotification;
	private boolean notifySuccess;
	private boolean notifyAborted;
	private boolean notifyNotBuilt;
	private boolean notifyUnstable;
	private boolean notifyFailure;
	private boolean notifyBackToNormal;
	private boolean notifyRepeatedFailure;
	private boolean sqsIncludeTestSummary;
	private boolean showCommitList;
	private boolean includeCustomSQSMessage;
	private String customSQSMessage;

	@DataBoundConstructor
	public SQSJobProperty(String teamDomain,
		String room,
		boolean startNotification,
		boolean notifyAborted,
		boolean notifyFailure,
		boolean notifyNotBuilt,
		boolean notifySuccess,
		boolean notifyUnstable,
		boolean notifyBackToNormal,
		boolean notifyRepeatedFailure,
		boolean sqsIncludeTestSummary,
		boolean showCommitList,
		boolean includeCustomSQSMessage,
		String customSQSMessage,
    String secretKeyId,
    String secretKey) {
	    this.endpoint = teamDomain;
	    this.room = room;
      this.secretKeyId = secretKeyId;
      this.secretKey = secretKey;
	    this.startNotification = startNotification;
	    this.notifyAborted = notifyAborted;
	    this.notifyFailure = notifyFailure;
	    this.notifyNotBuilt = notifyNotBuilt;
	    this.notifySuccess = notifySuccess;
	    this.notifyUnstable = notifyUnstable;
	    this.notifyBackToNormal = notifyBackToNormal;
	    this.notifyRepeatedFailure = notifyRepeatedFailure;
	    this.sqsIncludeTestSummary = sqsIncludeTestSummary;
	    this.showCommitList = showCommitList;
	    this.includeCustomSQSMessage = includeCustomSQSMessage;
	    this.customSQSMessage = customSQSMessage;
	}

	@Exported
	public String getEndpoint() {
	    return endpoint;
	}

	@Exported
	public String getRoom() {
	    return room;
	}

  @Exported
	public String getSecretKeyId() {
	    return secretKeyId;
	}

  @Exported
	public String getSecretKey() {
	    return secretKey;
	}

	@Exported
	public boolean getStartNotification() {
	    return startNotification;
	}

	@Exported
	public boolean getNotifySuccess() {
	    return notifySuccess;
	}

	@Exported
	public boolean getShowCommitList() {
	    return showCommitList;
	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
	    return super.prebuild(build, listener);
	}

	@Exported
	public boolean getNotifyAborted() {
	    return notifyAborted;
	}

	@Exported
	public boolean getNotifyFailure() {
	    return notifyFailure;
	}

	@Exported
	public boolean getNotifyNotBuilt() {
	    return notifyNotBuilt;
	}

	@Exported
	public boolean getNotifyUnstable() {
	    return notifyUnstable;
	}

	@Exported
	public boolean getNotifyBackToNormal() {
	    return notifyBackToNormal;
	}

	@Exported
	public boolean sqsIncludeTestSummary() {
	    return sqsIncludeTestSummary;
	}

	@Exported
	public boolean getNotifyRepeatedFailure() {
	    return notifyRepeatedFailure;
	}

	@Exported
	public boolean includeCustomSQSMessage() {
	    return includeCustomSQSMessage;
	}

	@Exported
	public String getCustomSQSMessage() {
	    return customSQSMessage;
	}

    }

    @Extension public static final class Migrator extends ItemListener {
	@SuppressWarnings("deprecation")
	@Override
	public void onLoaded() {
	    logger.info("Starting Settings Migration Process");
	    for (AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
		logger.info("processing Job: " + p.getName());

		final SQSJobProperty sqsJobProperty = p.getProperty(SQSJobProperty.class);

		if (sqsJobProperty == null) {
		    logger.info(String
			    .format("Configuration is already up to date for \"%s\", skipping migration",
				p.getName()));
		    continue;
		}

		SQSNotifier sqsNotifier = p.getPublishersList().get(SQSNotifier.class);

		if (sqsNotifier == null) {
		    logger.info(String
			    .format("Configuration does not have a notifier for \"%s\", not migrating settings",
				p.getName()));
		} else {

		    //map settings
		    if (StringUtils.isBlank(sqsNotifier.endpoint)) {
			sqsNotifier.endpoint = sqsJobProperty.getEndpoint();
		    }
		    if (StringUtils.isBlank(sqsNotifier.room)) {
			sqsNotifier.room = sqsJobProperty.getRoom();
		    }
        if (StringUtils.isBlank(sqsNotifier.secretKeyId)) {
			sqsNotifier.secretKeyId = sqsJobProperty.getSecretKeyId();
		    }
        if (StringUtils.isBlank(sqsNotifier.secretKey)) {
			sqsNotifier.secretKey = sqsJobProperty.getSecretKey();
		    }

		    sqsNotifier.startNotification = sqsJobProperty.getStartNotification();

		    sqsNotifier.notifyAborted = sqsJobProperty.getNotifyAborted();
		    sqsNotifier.notifyFailure = sqsJobProperty.getNotifyFailure();
		    sqsNotifier.notifyNotBuilt = sqsJobProperty.getNotifyNotBuilt();
		    sqsNotifier.notifySuccess = sqsJobProperty.getNotifySuccess();
		    sqsNotifier.notifyUnstable = sqsJobProperty.getNotifyUnstable();
		    sqsNotifier.notifyBackToNormal = sqsJobProperty.getNotifyBackToNormal();
		    sqsNotifier.notifyRepeatedFailure = sqsJobProperty.getNotifyRepeatedFailure();

		    sqsNotifier.sqsIncludeTestSummary = sqsJobProperty.sqsIncludeTestSummary();
		    sqsNotifier.sqsCommitInfoChoice = sqsJobProperty.getShowCommitList() ? CommitInfoChoice.AUTHORS_AND_TITLES : CommitInfoChoice.NONE;
		    sqsNotifier.includeCustomSQSMessage = sqsJobProperty.includeCustomSQSMessage();
		    sqsNotifier.customSQSMessage = sqsJobProperty.getCustomSQSMessage();
        sqsNotifier.room = sqsJobProperty.getRoom();
        sqsNotifier.endpoint = sqsJobProperty.getEndpoint();
		}

		try {
		    //property section is not used anymore - remove
		    p.removeProperty(SQSJobProperty.class);
		    p.save();
		    logger.info("Configuration updated successfully");
		} catch (IOException e) {
		    logger.log(Level.SEVERE, e.getMessage(), e);
		}
	    }
	}
    }
}
