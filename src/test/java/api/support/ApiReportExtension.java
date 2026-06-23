package api.support;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ApiReportExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        ApiReport.startTest(context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ApiReport.finishTest(context, context.getExecutionException().orElse(null));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ApiReport.writeReport();
    }
}
