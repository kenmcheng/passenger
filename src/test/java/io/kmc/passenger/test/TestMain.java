package io.kmc.passenger.test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import io.kmc.passenger.routine.Passage;

public class TestMain {
   public static void main(String[] args) throws Exception {
    
		Passage<TestObject> pipeline = Passage.create();

        var toMeasure = pipeline
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

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(pipeline);

        byte[] bytes = baos.toByteArray();
        System.out.println("Length of serialized pipeline: " + bytes.length);
		System.out.println("Pipeline size1: " + ObjectSizeFetcher.getObjectSize(bytes));
        System.out.println("Pipeline size2: " + ObjectSizeFetcher.getObjectSize(toMeasure));
   } 
}
