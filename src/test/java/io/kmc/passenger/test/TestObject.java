package io.kmc.passenger.test;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestObject implements Serializable{

    private static Logger log = LoggerFactory.getLogger(PipelineTest.class);
   
    public void method1() {
        log.info("method1 invoked!");
    }

    public void method2() {
        log.info("method2 invoked!");
    }

    public String method3() {
        log.info("method3 invoked!");
        return "return method3";
    }

    public boolean method4() {
        log.info("method4 invoked!");
        return false;
    }

    public void method5() {
        log.info("method5 invoked!");
    }

    public boolean method6() {
        log.info("method6 invoked!");
        return true;
    }

    public void method7() {
        log.info("method7 invoked!");
    }

    public void method8() {
        log.info("method8 invoked!");
    }

    public void method9() {
        log.info("method9 invoked!");
    }

    public void method10() {
        log.info("method10 invoked!");
    }

    public void method11() {
        log.info("method11 invoked!");
    }
    public void method12() {
        log.info("method12 invoked!");
    }

    public String method13() {
        log.info("method13 invoked!");
        return "return method3";
    }

    public boolean method14() {
        log.info("method14 invoked!");
        return false;
    }

    public boolean method15() {
        log.info("method15 invoked!");
        return true;
    }

    public void method16(String s) {
        log.info("method16 with string invoked! input = " + s);
    }

    public void method16(Integer num) {
        log.info("method16 with integer invoked! input = " + num);
    }

    public void method17(String s, Integer num) {
        log.info("method17 invoked! inputs = " + s + ", " + num);
    }
}
