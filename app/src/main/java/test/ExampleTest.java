package test;

import android.test.InstrumentationTestCase;

/**
 * Created by louismagarshack on 10/8/14.
 */
public class ExampleTest extends InstrumentationTestCase {


    public void testFail() throws Exception {
        int expected = 1;
        int reality = 5;

        assertEquals(expected, reality);

    }
}
