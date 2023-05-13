package io.kmc.passenger.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtilObject {
    
    private static Logger log = LoggerFactory.getLogger(PipelineTest.class);
    
    public static void method1(TestObject obj) {
        log.info("TestUtilObject method1 invoked!");
    }

    public void method2(TestObject obj) {
        log.info("TestUtilObject method2 invoked!");
    }

    public void method3(TestObject obj, String s, Integer num) {
        log.info("TestUtilObject method3 invoked!");
    }
}
