package jenkins.plugins.sqs;

import hudson.ProxyConfiguration;
import org.apache.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
@Ignore
public class StandardSQSServiceTest {

	/**
	 * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
	 */
	@Test
	public void publishWithBadHostShouldNotRethrowExceptions() {
		StandardSQSService service = new StandardSQSService("foo", "#general", "bar", "baz");
		service.setEndpoint("hostvaluethatwillcausepublishtofail");
		service.publish("message");
	}

	/**
	 * Use a valid host, but an invalid team domain
	 */
	@Test
	public void invalidHostShouldFail() {
		StandardSQSService service = new StandardSQSService("my", "#general", "bar", "baz");
		service.publish("message");
	}

	@Test
	public void publishToASingleRoomSendsASingleMessage() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "#room1", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		service.setHttpClient(httpClientStub);
		service.publish("message");
		assertEquals(1, service.getHttpClient().getNumberOfCallsToExecuteMethod());
	}

	@Test
	public void publishToMultipleRoomsSendsAMessageToEveryRoom() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "#room1,#room2,#room3", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		service.setHttpClient(httpClientStub);
		service.publish("message");
		assertEquals(3, service.getHttpClient().getNumberOfCallsToExecuteMethod());
	}

	@Test
	public void successfulPublishToASingleRoomReturnsTrue() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "#room1", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		httpClientStub.setHttpStatus(HttpStatus.SC_OK);
		service.setHttpClient(httpClientStub);
		assertTrue(service.publish("message"));
	}

	@Test
	public void successfulPublishToMultipleRoomsReturnsTrue() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "#room1,#room2,#room3", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		httpClientStub.setHttpStatus(HttpStatus.SC_OK);
		service.setHttpClient(httpClientStub);
		assertTrue(service.publish("message"));
	}

	@Test
	public void failedPublishToASingleRoomReturnsFalse() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "#room1", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		httpClientStub.setHttpStatus(HttpStatus.SC_NOT_FOUND);
		service.setHttpClient(httpClientStub);
		assertFalse(service.publish("message"));
	}

	@Test
	public void singleFailedPublishToMultipleRoomsReturnsFalse() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "#room1,#room2,#room3", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		httpClientStub.setFailAlternateResponses(true);
		httpClientStub.setHttpStatus(HttpStatus.SC_OK);
		service.setHttpClient(httpClientStub);
		assertFalse(service.publish("message"));
	}

	@Test
	public void publishToEmptyRoomReturnsTrue() {
		StandardSQSServiceStub service = new StandardSQSServiceStub("domain", "", "bar", "baz");
		HttpClientStub httpClientStub = new HttpClientStub();
		httpClientStub.setHttpStatus(HttpStatus.SC_OK);
		service.setHttpClient(httpClientStub);
		assertTrue(service.publish("message"));
	}

	@Test
	public void isProxyRequiredEmtyNoProxyHostsReturnsTrue() {
		StandardSQSService service = new StandardSQSService("http://mysqs.endpoint.com","roomid", "bar", "baz");
		assertTrue(service.isProxyRequired(Collections.<Pattern>emptyList()));
	}

	@Test
	public void isProxyRequiredNoProxyHostsDoesNotMatchReturnsTrue() {
		StandardSQSService service = new StandardSQSService("http://mysqs.endpoint.com","roomid", "bar", "baz");
		assertTrue(service.isProxyRequired(ProxyConfiguration.getNoProxyHostPatterns("*.internal.com")));
	}

	@Test
	public void isProxyRequiredNoProxyHostsMatchReturnsFalse() {
		StandardSQSService service = new StandardSQSService("http://mysqs.endpoint.com","roomid", "bar", "baz");
		assertFalse(service.isProxyRequired(ProxyConfiguration.getNoProxyHostPatterns("*.endpoint.com")));
	}

	@Test
	public void isProxyRequiredInvalidEndPointReturnsTrue() {
		StandardSQSService service = new StandardSQSService("htt://mysqs.endpoint.com","roomid", "bar", "baz");
		assertTrue(service.isProxyRequired(ProxyConfiguration.getNoProxyHostPatterns("*.internal.com")));
	}


}
