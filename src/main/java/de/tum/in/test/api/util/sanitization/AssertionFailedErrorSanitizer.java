package de.tum.in.test.api.util.sanitization;

import static de.tum.in.test.api.util.BlacklistedInvoker.invoke;

import java.util.Set;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

enum AssertionFailedErrorSanitizer implements SpecificThrowableSanitizer {
	INSTANCE;

	private final Set<Class<? extends Throwable>> types = Set.of(AssertionFailedError.class);

	@Override
	public boolean canSanitize(Throwable t) {
		return types.contains(t.getClass());
	}

	@Override
	public Throwable sanitize(Throwable t) throws SanitizationError {
		AssertionFailedError afe = (AssertionFailedError) t;
		ValueWrapper expected = invoke(afe::getExpected);
		ValueWrapper actual = invoke(afe::getActual);
		if (expected == null && actual == null)
			return SimpleThrowableSanitizer.INSTANCE.sanitize(afe);
		AssertionFailedError newAfe = new AssertionFailedError(invoke(afe::getMessage), sanitizeValue(expected),
				sanitizeValue(actual));
		ThrowableSanitizer.copyThrowableInfo(afe, newAfe);
		return newAfe;
	}

	private static Object sanitizeValue(ValueWrapper vw) {
		if (vw == null)
			return null;
		return invoke(vw::getStringRepresentation);
	}
}
