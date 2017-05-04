package jenkins.plugins.sqs;

public interface SQSService {
	boolean publish(String message);

	boolean publish(String message, String color);
}
