package io.kmc.passenger.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kmc.passenger.handler.Target;
import io.kmc.passenger.routine.Passage;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PipelineTest {

    private static Logger log = LoggerFactory.getLogger(PipelineTest.class);

    long timer = 0L;

    @Mock
    static TestObject mockedObj;

    @BeforeAll
    public static void printObjectInfo() throws Exception {
        
    }

    @BeforeEach
    public void setMock() {
        mockedObj = Mockito.mock(TestObject.class);
        Mockito.when(mockedObj.method4()).thenReturn(false);
        Mockito.when(mockedObj.method6()).thenReturn(true);
    }

    @BeforeEach
    public void timerStart() {
        timer = System.currentTimeMillis();
    }

    @AfterEach
    public void timerStop() {
        System.out.println("\nTime spent: " + (System.currentTimeMillis() - timer) + " ms");
    }

    @Test
    @Order(0)
    public void directlyExecuteMethods() throws Exception {
        // TestObject mockedObj = mock(TestObject.class);

        mockedObj.method1();
        mockedObj.method2();
        mockedObj.method3();
        mockedObj.method4();
        mockedObj.method5();
        mockedObj.method6();
        mockedObj.method7();
        mockedObj.method8();
        mockedObj.method9();
        mockedObj.method10();
        mockedObj.method11();

        Mockito.verify(mockedObj).method1();
        Mockito.verify(mockedObj).method2();
        Mockito.verify(mockedObj).method3();
        Mockito.verify(mockedObj).method4();
        Mockito.verify(mockedObj).method5();
        Mockito.verify(mockedObj).method6();
        Mockito.verify(mockedObj).method7();
        Mockito.verify(mockedObj).method8();
        Mockito.verify(mockedObj).method9();
        Mockito.verify(mockedObj).method10();
        Mockito.verify(mockedObj).method11();
    }

    @Test
    @Order(1)
    public void  preLoadClass() throws Exception {
        Passage<TestObject> pipeline1 = Passage.create();

        pipeline1
                .add("io.kmc.passenger.test.TestObject","method1")
                .add("io.kmc.passenger.test.TestObject","method2")
                .add("io.kmc.passenger.test.TestObject","method3").toLast()
                .whenever(TestObject::method4) // false
                    .perform(TestObject::method5)
                .elsewhen(TestObject::method6) // true
                    .perform(TestObject::method7)
                    .perform("io.kmc.passenger.test.TestObject","method8")
                .otherwise()
                    .perform(TestObject::method9)
                .endIf().toFirst()
                .add(TestObject::method10).toLast()
                .add(TestObject::method11)
                .add("io.kmc.passenger.test.TestObject","method16", "test input")
                .add("io.kmc.passenger.test.TestObject","method16", 6)
                .add("io.kmc.passenger.test.TestObject","method17", "test input", 7)
                .add("io.kmc.passenger.test.TestUtilObject","method1", new Target())
                .add("io.kmc.passenger.test.TestUtilObject","method2", new Target())
                .add("io.kmc.passenger.test.TestUtilObject","method3", new Target(), "test input", 3)
                ;

        pipeline1.applyTo(new TestObject()).run();
    }

    // @Disabled
    @Test
    @Order(12)
    public void createAction() throws Exception {
        // Mockito.when(mockedObj.method4()).thenReturn(false);
        // Mockito.when(mockedObj.method6()).thenReturn(true);

        Passage<TestObject> pipeline = Passage.create();

        pipeline
                .add("io.kmc.passenger.test.TestObject","method1")
                .add("io.kmc.passenger.test.TestObject","method2")
                .add("io.kmc.passenger.test.TestObject","method3").toLast()
                .whenever(TestObject::method4) // false
                    .perform(TestObject::method5)
                .elsewhen(TestObject::method6) // true
                    .perform(TestObject::method7)
                    .perform("io.kmc.passenger.test.TestObject","method8")
                .otherwise()
                    .perform(TestObject::method9)
                .endIf().toFirst()
                .add(TestObject::method10).toLast()
                .add(TestObject::method11)
                ;

        InOrder inOrder = Mockito.inOrder(mockedObj);

        pipeline.applyTo(mockedObj).run();

        inOrder.verify(mockedObj).method4();
        inOrder.verify(mockedObj).method6();
        inOrder.verify(mockedObj).method7();
        inOrder.verify(mockedObj).method8();
        inOrder.verify(mockedObj).method1();
        inOrder.verify(mockedObj).method2();
        inOrder.verify(mockedObj).method11();
        inOrder.verify(mockedObj).method3();
        inOrder.verify(mockedObj).method10();

        // second round
        pipeline.applyTo(mockedObj).run();

        inOrder.verify(mockedObj).method4();
        inOrder.verify(mockedObj).method6();
        inOrder.verify(mockedObj).method7();
        inOrder.verify(mockedObj).method8();
        inOrder.verify(mockedObj).method1();
        inOrder.verify(mockedObj).method2();
        inOrder.verify(mockedObj).method11();
        inOrder.verify(mockedObj).method3();
        inOrder.verify(mockedObj).method10();
    }

    @Test
    @Order(13)
    public void concatActions() throws Exception {
        Passage<TestObject> pipeline1 = Passage.create();

        pipeline1
                .add( obj -> {log.info(">> pipeline 1 starts!");})
                .add(TestObject::method1)
                .add(TestObject::method2)
                .add(TestObject::method3).toLast()
                .whenever(TestObject::method4) // false
                    .perform(TestObject::method5)
                .elsewhen(TestObject::method6) // true
                    .perform(TestObject::method7)
                    .perform(TestObject::method8)
                .otherwise()
                    .perform(TestObject::method9)
                .endIf().toFirst()
                .add(TestObject::method10).toLast()
                .add(TestObject::method11)
                ;

        Passage<TestObject> pipeline2 = Passage.create();
       
        System.out.printf("Pipeline 1 composed : %d ms\n", System.currentTimeMillis() - timer);

        pipeline2
                .add( obj -> {log.info(">> pipeline 2 starts!");})
                .add(TestObject::method5)
                .add(TestObject::method6).toFirst()
                ;

        System.out.printf("Pipeline 2 composed : %d ms\n", System.currentTimeMillis() - timer);

        Passage<TestObject> pipeline3 = Passage.create();
        pipeline3
                .add( obj -> {log.info(">> pipeline 3 starts!");})
                .add(TestObject::method9)
                .add(TestObject::method10)
                ;

        System.out.printf("Pipeline 3 composed : %d ms\n", System.currentTimeMillis() - timer);

        pipeline1.integrate(pipeline2).integrate(pipeline3);
        // pipeline1.join(pipeline3);

        System.out.printf("Pipelines joined: %d ms\n", System.currentTimeMillis() - timer);

        pipeline1
                .add( obj -> {log.info(">> additional actions start!");})
                .add(TestObject::method12).toFirst()
                .add(TestObject::method13)
                ;

        System.out.printf("Pipeline added : %d ms\n", System.currentTimeMillis() - timer);
        
        pipeline1.applyTo(new TestObject()).run();
        // log.info("\n>>> Second round <<<");
        // pipeline1.applyTo(new TestObject()).run();

    }


    @Test
    @Order(14)
    public void  createReflect() throws Exception {
        Passage<TestObject> pipeline1 = Passage.create();

        pipeline1
                .add("io.kmc.passenger.test.TestObject","method1")
                .add("io.kmc.passenger.test.TestObject","method2")
                .add("io.kmc.passenger.test.TestObject","method3")
                .add("io.kmc.passenger.test.TestObject","method4")
                .add("io.kmc.passenger.test.TestObject","method5")
                .add("io.kmc.passenger.test.TestObject","method6")
                .add("io.kmc.passenger.test.TestObject","method7")
                .add("io.kmc.passenger.test.TestObject","method8")
                .add("io.kmc.passenger.test.TestObject","method9")
                .add("io.kmc.passenger.test.TestObject","method10")
                .add("io.kmc.passenger.test.TestObject","method11")
                .add("io.kmc.passenger.test.TestObject","method12")
                .add("io.kmc.passenger.test.TestObject","method16", "test input")
                .add("io.kmc.passenger.test.TestObject","method17", "test input", 5)
                .add("io.kmc.passenger.test.TestObject","method1")
                .add("io.kmc.passenger.test.TestUtilObject","method1", new Target())
                .add("io.kmc.passenger.test.TestUtilObject","method2", new Target())
                ;

        pipeline1.applyTo(mockedObj).run();

        Mockito.verify(mockedObj, Mockito.times(2)).method1();
        Mockito.verify(mockedObj).method2();
        Mockito.verify(mockedObj).method3();
        Mockito.verify(mockedObj).method4();
        Mockito.verify(mockedObj).method5();
        Mockito.verify(mockedObj).method6();
        Mockito.verify(mockedObj).method7();
        Mockito.verify(mockedObj).method8();
        Mockito.verify(mockedObj).method9();
        Mockito.verify(mockedObj).method10();
        Mockito.verify(mockedObj).method11();
        Mockito.verify(mockedObj).method12();
        Mockito.verify(mockedObj).method16("test input");
        Mockito.verify(mockedObj).method17("test input", 5);


    }

    @Test
    @Order(20)
    public void prerun() throws Exception {
        TestObject testObject = Mockito.mock(TestObject.class);
        int cnt = 0;
        while (cnt++ < 1000) {
            testObject.method1();
            testObject.method2();
            testObject.method3();
            testObject.method4();
            testObject.method5();
            testObject.method6();
            testObject.method7();
            testObject.method8();
            testObject.method9();
            testObject.method10();
            testObject.method11();
            testObject.method12();
            testObject.method16("test input");
            testObject.method17("test input", 5);
        }
    }

    @Test
    @Order(21)
    public void reference() throws Exception {
        TestObject testObject = Mockito.mock(TestObject.class);
        int cnt = 0;
        while (cnt++ < 10000) {
            testObject.method1();
            testObject.method2();
            testObject.method3();
            testObject.method4();
            testObject.method5();
            testObject.method6();
            testObject.method7();
            testObject.method8();
            testObject.method9();
            testObject.method10();
            testObject.method11();
            testObject.method12();
            // testObject.method16("test input");
            // testObject.method17("test input", 5);
        }
    }
    
    @Test
    @Order(22)
    public void lambdaPerformanceTest() throws Exception {
        Passage<TestObject> pipeline1 = Passage.create();

        pipeline1
                .add(TestObject::method1)
                .add(TestObject::method2)
                .add(TestObject::method3)
                .add(TestObject::method4) // false
                .add(TestObject::method5)
                .add(TestObject::method6) // true
                .add(TestObject::method7)
                .add(TestObject::method8)
                .add(TestObject::method9)
                .add(TestObject::method10)
                .add(TestObject::method11)
                .add(TestObject::method12)
                ;

        TestObject testObject = Mockito.mock(TestObject.class);
        int cnt = 0;
        int totalRun = 10000;
        while (cnt++ < totalRun) {
            pipeline1.applyTo(testObject).run();
        }
    }

    @Test
    @Order(23)
    public void  reflectPerformanceTest() throws Exception {
        Passage<TestObject> pipeline1 = Passage.create();

        pipeline1
                .add("io.kmc.passenger.test.TestObject","method1")
                .add("io.kmc.passenger.test.TestObject","method2")
                .add("io.kmc.passenger.test.TestObject","method3")
                .add("io.kmc.passenger.test.TestObject","method4")
                .add("io.kmc.passenger.test.TestObject","method5")
                .add("io.kmc.passenger.test.TestObject","method6")
                .add("io.kmc.passenger.test.TestObject","method7")
                .add("io.kmc.passenger.test.TestObject","method8")
                .add("io.kmc.passenger.test.TestObject","method9")
                .add("io.kmc.passenger.test.TestObject","method10")
                .add("io.kmc.passenger.test.TestObject","method11")
                .add("io.kmc.passenger.test.TestObject","method12")
                // .add("io.kmc.passenger.test.TestObject","method16", "test input")
                // .add("io.kmc.passenger.test.TestObject","method17", "test input", 5)
                ;
        
        TestObject testObject = Mockito.mock(TestObject.class);
        int cnt = 0;
        int totalRun = 10000;
        while (cnt++ < totalRun) {
            pipeline1.applyTo(testObject).run();
        }
        
    }
}
