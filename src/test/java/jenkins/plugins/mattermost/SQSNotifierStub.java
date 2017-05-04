package jenkins.plugins.sqs;

public class SQSNotifierStub extends SQSNotifier {

	public SQSNotifierStub(String host, String room, String buildServerUrl,
			String sendAs, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
			boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyBackToNormal,
			boolean notifyRepeatedFailure, boolean includeTestSummary, CommitInfoChoice commitInfoChoice,
			boolean includeCustomMessage, String customMessage, String secretKeyId, String secretKey) {
		super(host, room, buildServerUrl, sendAs, startNotification, notifyAborted, notifyFailure,
				notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
				includeTestSummary, commitInfoChoice, includeCustomMessage, customMessage, secretKeyId, secretKey);
	}

	public static class DescriptorImplStub extends SQSNotifier.DescriptorImpl {

		private SQSService sqsService;

		@Override
		public synchronized void load() {
		}

		@Override
		SQSService getSQSService(final String host, final String room, final String secretKeyId, final String secretKey) {
			return sqsService;
		}

		public void setSQSService(SQSService sqsService) {
			this.sqsService = sqsService;
		}
	}
}
