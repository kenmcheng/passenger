package io.kmc.passenger.routine

import java.io.Serializable
import java.util.*


interface Condition<T> : Serializable {

    fun elsewhen(condStmt: SerializablePredicate<T>): Condition<T>

    fun otherwise(): Condition<T>

    fun perform(clazzName: String, methodName: String, vararg args: Serializable): Condition<T>

//    fun perform(clazzName: String, methodName: String, targetIndex: Int?, vararg args: Serializable): Condition<T>

    fun perform(func: SerializableConsumer<in T>): Condition<T>

    fun <R> perform(func: SerializableFunction<in T, out R?>): Condition<T>

    fun endIf(): RoutinePipeline<T>?

    @Throws(Exception::class)
    fun execute(target: T)

    fun reset()
    
    companion object {
        fun <T> create(condStmt: SerializablePredicate<T>): Condition<T> {
            return object : AbstractCondition<T>() {
                init {
                    statements.add(condStmt)
                    // routineGroups.add(Passage.create());
                    routineGroups.add(PipelineEndpoint.create(null))
                    index = 0
                }
            }
        }
    }

    abstract class AbstractCondition<T> protected constructor() : Condition<T> {
        var statements: MutableList<SerializablePredicate<T>> = LinkedList()
        var routineGroups: MutableList<Passage<T>> = ArrayList()
        var index = 0

        override fun endIf(): RoutinePipeline<T>? {
            // End of conditional statement, do nothing
            return null
        }

        override fun elsewhen(condStmt: SerializablePredicate<T>): Condition<T> {
            statements.add(condStmt)
            // routineGroups.add(Passage.create());
            routineGroups.add(PipelineEndpoint.create<T>(null))
            index++
            return this
        }

        override fun otherwise(): Condition<T> {
            this.elsewhen { t: T -> true }
            return this
        }

        override fun perform(clazzName: String, methodName: String, vararg args: Serializable): Condition<T> {
            routineGroups[index].add(clazzName!!, methodName!!, *args)
            return this
        }

//        override fun perform(clazzName: String, methodName: String, targetIndex: Int?, vararg args: Serializable): Condition<T> {
//            routineGroups[index].add(clazzName!!, methodName!!, targetIndex!!, *args)
//            return this
//        }

        override fun perform(func: SerializableConsumer<in T>): Condition<T> {
            routineGroups[index].add(func)
            return this
        }

        override fun <R> perform(func: SerializableFunction<in T, out R?>): Condition<T> {
            routineGroups[index].add(func)
            return this
        }

        @Throws(Exception::class)
        override fun execute(target: T) {
            var entry = 0
            for (stmt in statements) {
                if (stmt.test(target)) {
                    routineGroups[entry].execute(target)
                    break
                }
                entry++
            }
        }

        override fun reset() {
            for (routine in routineGroups) {
                routine.reset()
            }
        }
    }
}