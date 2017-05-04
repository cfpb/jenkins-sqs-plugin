package jenkins.plugins.sqs;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SQSNotifierTest extends TestCase {

	private SQSNotifierStub.DescriptorImplStub descriptor;
	private SQSServiceStub sqsServiceStub;
	private boolean response;
	private FormValidation.Kind expectedResult;

	@Before
	@Override
	public void setUp() {
		descriptor = new SQSNotifierStub.DescriptorImplStub();
	}

	public SQSNotifierTest(SQSServiceStub sqsServiceStub, boolean response, FormValidation.Kind expectedResult) {
		this.sqsServiceStub = sqsServiceStub;
		this.response = response;
		this.expectedResult = expectedResult;
	}

	@Parameterized.Parameters
	public static Collection businessTypeKeys() {
		return Arrays.asList(new Object[][]{
			{new SQSServiceStub(), true, FormValidation.Kind.OK},
				{new SQSServiceStub(), false, FormValidation.Kind.ERROR},
				{null, false, FormValidation.Kind.ERROR}
		});
	}

	@Test
	public void testDoTestConnection() {
		if (sqsServiceStub != null) {
			sqsServiceStub.setResponse(response);
		}
		descriptor.setSQSService(sqsServiceStub);
		try {
			FormValidation result = descriptor.doTestConnection("host", "room", "buildServerUrl");
			assertEquals(result.kind, expectedResult);
		} catch (Descriptor.FormException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public static class SQSServiceStub implements SQSService {

		private boolean response;

		public boolean publish(String message) {
			return response;
		}

		public boolean publish(String message, String color) {
			return response;
		}

		public void setResponse(boolean response) {
			this.response = response;
		}
	}
}
