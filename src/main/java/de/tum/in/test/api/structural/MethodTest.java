package de.tum.in.test.api.structural;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;

/**
 * @author Stephan Krusche (krusche@in.tum.de)
 * @version 5.0 (11.11.2020)
 * <br><br>
 * This test evaluates if the specified methods in the structure oracle are correctly implemented with the expected name, return type, parameter types, visibility modifiers
 * and annotations, based on its definition in the structure oracle (test.json)
 */
public abstract class MethodTest extends StructuralTest {

    /**
     * This method collects the classes in the structure oracle file for which methods are specified.
     * These classes are then transformed into JUnit 5 dynamic tests.
     * @return A dynamic test container containing the test for each class which is then executed by JUnit.
     * @throws URISyntaxException an exception if the URI of the class name cannot be generated (which seems to be unlikely)
     */
    protected DynamicContainer generateTestsForAllClasses() throws URISyntaxException {
        List<DynamicNode> tests = new ArrayList<>();

        if (structureOracleJSON == null) {
            fail("The LocalMethodTest test can only run if the structural oracle (test.json) is present. If you do not provide it, delete LocalMethodTest.java!");
        }

        for (int i = 0; i < structureOracleJSON.length(); i++) {
            JSONObject expectedClassJSON = structureOracleJSON.getJSONObject(i);

            // Only test the classes that have methods defined in the structure oracle.
            if (expectedClassJSON.has(JSON_PROPERTY_CLASS) && expectedClassJSON.has(JSON_PROPERTY_METHODS)) {
                JSONObject expectedClassPropertiesJSON = expectedClassJSON.getJSONObject(JSON_PROPERTY_CLASS);
                String expectedClassName = expectedClassPropertiesJSON.getString(JSON_PROPERTY_NAME);
                String expectedPackageName = expectedClassPropertiesJSON.getString(JSON_PROPERTY_PACKAGE);
                ExpectedClassStructure expectedClassStructure = new ExpectedClassStructure(expectedClassName, expectedPackageName, expectedClassJSON);
                tests.add(dynamicTest("testMethods[" + expectedClassName + "]", () -> testMethods(expectedClassStructure)));
            }
        }
        if (tests.isEmpty()) {
            fail("No tests for methods available in the structural oracle (test.json). Either provide attributes information or delete MethodTest.java!");
        }
        // Using a custom URI here to workaround surefire rendering the JUnit XML without the correct test names.
        return dynamicContainer(getClass().getName(), new URI(getClass().getName()), tests.stream());
    }

    /**
     * This method gets passed the expected class structure generated by the method generateTestsForAllClasses(), checks if the class is found
     * at all in the assignment and then proceeds to check its methods.
     * @param expectedClassStructure: The class structure that we expect to find and test against.
     */
    public void testMethods(ExpectedClassStructure expectedClassStructure) {
        String expectedClassName = expectedClassStructure.getExpectedClassName();
        Class<?> observedClass = findClassForTestType(expectedClassStructure, "method");
        if (observedClass == null) {
            fail(THE_CLASS + expectedClassName + " was not found for method test");
            return;
        }

        if (expectedClassStructure.hasProperty(JSON_PROPERTY_METHODS)) {
            JSONArray methodsJSON = expectedClassStructure.getPropertyAsJsonArray(JSON_PROPERTY_METHODS);
            checkMethods(expectedClassName, observedClass, methodsJSON);
        }
    }

    /**
     * This method checks if a observed class' methods match the expected ones defined in the structure oracle.
     * @param expectedClassName: The simple name of the class, mainly used for error messages.
     * @param observedClass: The class that needs to be checked as a Class object.
     * @param expectedMethods: The information on the expected methods contained in a JSON array. This information consists
     * of the name, parameter types, return type and the visibility modifiers of each method.
     */
    protected void checkMethods(String expectedClassName, Class<?> observedClass, JSONArray expectedMethods) {
        for(int i = 0; i < expectedMethods.length(); i++) {
            JSONObject expectedMethod = expectedMethods.getJSONObject(i);
            String expectedName = expectedMethod.getString(JSON_PROPERTY_NAME);
            JSONArray expectedParameters = getExpectedJsonProperty(expectedMethod, JSON_PROPERTY_PARAMETERS);
            JSONArray expectedModifiers = getExpectedJsonProperty(expectedMethod, JSON_PROPERTY_MODIFIERS);
            JSONArray expectedAnnotations = getExpectedJsonProperty(expectedMethod, JSON_PROPERTY_ANNOTATIONS);
            String expectedReturnType = expectedMethod.getString(JSON_PROPERTY_RETURN_TYPE);

            boolean nameIsCorrect = false;
            boolean parametersAreCorrect = false;
            boolean modifiersAreCorrect = false;
            boolean returnTypeIsCorrect = false;
            boolean annotationsAreCorrect = false;

            for(Method observedMethod : observedClass.getDeclaredMethods()) {
                String observedName = observedMethod.getName();
                Class<?>[] observedParameters = observedMethod.getParameterTypes();
                String[] observedModifiers = Modifier.toString(observedMethod.getModifiers()).split(" ");
                String observedReturnType = observedMethod.getReturnType().getSimpleName();
                Annotation[] observedAnnotations = observedMethod.getAnnotations();

                // If the names don't match, then proceed to the next observed method
                if(!expectedName.equals(observedName)) {
                    //TODO: we should also take wrong case and typos into account
                    //TODO: check if overloading is supported properly
                    continue;
                } else {
                    nameIsCorrect = true;
                }

                parametersAreCorrect = checkParameters(observedParameters, expectedParameters);
                modifiersAreCorrect = checkModifiers(observedModifiers, expectedModifiers);
                annotationsAreCorrect = checkAnnotations(observedAnnotations, expectedAnnotations);
                returnTypeIsCorrect = expectedReturnType.equals(observedReturnType);

                // If all are correct, then we found our method and we can break the loop
                if(parametersAreCorrect && modifiersAreCorrect && annotationsAreCorrect && returnTypeIsCorrect) {
                    break;
                }
            }

            checkMethodCorrectness(expectedClassName, expectedName, expectedParameters, nameIsCorrect, parametersAreCorrect, modifiersAreCorrect, returnTypeIsCorrect, annotationsAreCorrect);
        }
    }

    private void checkMethodCorrectness(String expectedClassName, String expectedName, JSONArray expectedParameters, boolean nameIsCorrect, boolean parametersAreCorrect,
            boolean modifiersAreCorrect, boolean returnTypeIsCorrect, boolean annotationsAreCorrect) {
        String expectedMethodInformation = "the expected method '" + expectedName + "' of the class '" + expectedClassName + "' with "
                + ((expectedParameters.length() == 0) ? "no parameters" : "the parameters: " + expectedParameters.toString());

        if (!nameIsCorrect) {
            fail(expectedMethodInformation + " was not found or is named wrongly.");
        }
        if (!parametersAreCorrect) {
            fail("The parameters of " + expectedMethodInformation + NOT_IMPLEMENTED_AS_EXPECTED);
        }
        if (!modifiersAreCorrect) {
            fail("The modifiers (access type, abstract, etc.) of " + expectedMethodInformation + NOT_IMPLEMENTED_AS_EXPECTED);
        }
        if (!annotationsAreCorrect) {
            fail("The annotation(s) of " + expectedMethodInformation + NOT_IMPLEMENTED_AS_EXPECTED);
        }
        if (!returnTypeIsCorrect) {
            fail("The return type of " + expectedMethodInformation + " is not implemented as expected.");
        }
    }
}
