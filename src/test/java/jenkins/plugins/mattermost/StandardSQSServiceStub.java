package jenkins.plugins.sqs;

public class StandardSQSServiceStub extends StandardSQSService {

	private HttpClientStub httpClientStub;

	public StandardSQSServiceStub(String host, String roomId, String secretKeyId, String secretKey) {
		super(host, roomId, secretKeyId, secretKey);
	}

	@Override
	public HttpClientStub getHttpClient() {
		return httpClientStub;
	}

	public void setHttpClient(HttpClientStub httpClientStub) {
		this.httpClientStub = httpClientStub;
	}
}
