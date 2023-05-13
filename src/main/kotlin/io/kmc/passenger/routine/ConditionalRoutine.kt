package io.kmc.passenger.routine

import java.io.Serializable

class ConditionalRoutine<T> : AbstractRoutinePipeline<T>, Condition<T> {

    var condition: Condition<T>

    constructor(
            previousAction: AbstractRoutinePipeline<T>,
            condStmt: SerializablePredicate<T>
    ) : this(previousAction, Condition.create(condStmt)) {}

    constructor(previousAction: AbstractRoutinePipeline<T>,
                condition: Condition<T>
    ) : super(previousAction) {
        this.condition = condition
    }

    override fun endIf(): RoutinePipeline<T> {
        condition!!.endIf()
        return this
    }

    override fun elsewhen(condStmt: SerializablePredicate<T>): Condition<T> {
        condition!!.elsewhen(condStmt)
        return this
    }

    override fun otherwise(): Condition<T> {
        condition!!.otherwise()
        return this
    }

    override fun perform(clazzName: String, methodName: String, vararg args: Serializable): Condition<T> {
        condition!!.perform(clazzName, methodName, *args)
        return this
    }

//    override fun perform(clazzName: String, methodName: String, targetIndex: Int, vararg args: Serializable): Condition<T> {
//        condition!!.perform(clazzName, methodName, targetIndex, *args)
//        return this
//    }

    override fun perform(func: SerializableConsumer<in T>): Condition<T> {
        condition!!.perform(func)
        return this
    }

    override fun <R> perform(func: SerializableFunction<in T, out R?>): Condition<T> {
        condition!!.perform(func)
        return this
    }

    @Throws(Exception::class)
    override fun _execute(target: T) {
        condition!!.execute(target)
    }

    override fun reset() {
        super.reset()
        condition!!.reset()
    }

    @Throws(Exception::class)
    override fun run() {
        root?.run()
    }
}