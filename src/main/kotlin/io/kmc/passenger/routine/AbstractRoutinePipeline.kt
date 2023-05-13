package io.kmc.passenger.routine

import java.io.Serializable

import io.kmc.passenger.handler.Target

abstract class AbstractRoutinePipeline<T> : RoutinePipeline<T> {

    var root: PipelineEndpoint<T>? = null

    var previous: RoutinePipeline<T>? = null

    var next: RoutinePipeline<T>? = null

    var executed = false

    protected constructor () {}

    protected constructor(previousRoutine: AbstractRoutinePipeline<T>) {
        next = previousRoutine.next
        previous = previousRoutine
        previousRoutine.next = this
        if (next != null) next!!.attach(this)
        this.root = previousRoutine.root
    }

    override fun applyTo(target: T?): Passage<T> {
        root!!.applyTo(target)
        return this
    }

    @Throws(Exception::class)
    override fun execute(target: T) {
        if (executed) return
        _execute(target)
        executed = true
        if (hasNext()) {
            next!!.execute(target)
        }
    }

    @Throws(Exception::class)
    protected abstract fun _execute(target: T)

    override fun reset() {
        if (!executed) return
        executed = false
        if (hasNext()) {
            next!!.reset()
        }
    }

    override fun add(routine: RoutinePipeline<T>): RoutinePipeline<T> {
        insert(routine)
        routine.changeRoot(root)
        return routine
    }

    override fun join(passage: Passage<T>): RoutinePipeline<T> {
        return JointRoutine<T>(this, passage)
    }

    override fun integrate(passage: Passage<T>): Passage<T> {
        return if (root != null) root!!.integrate(passage) else this
    }

    override fun add(clazzName: String, methodName: String, vararg args: Serializable): ReflectRoutine<T> {
        var targetIndex = -1
        for (i in args.indices) {
            if (args[i] is Target) {
                targetIndex = i
                break
            }
        }
        return addReflect(clazzName, methodName, targetIndex, *args)
    }

    private fun addReflect(
        clazzName: String,
        methodName: String = "",
        targetIndex: Int = -1,
        vararg args: Serializable
    ): ReflectRoutine<T> {
        return ReflectRoutine(this, clazzName, methodName, targetIndex, *args)
    }

    override fun <R> add(func: SerializableFunction<in T, out R?>): RoutinePipeline<T> {
        return object : AbstractRoutinePipeline<T>(this) {
            @Throws(Exception::class)
            override fun run() {
                root!!.run()
            }

            @Throws(Exception::class)
            override fun _execute(target: T) {
                func.apply(target)
            }
        }
    }

    override fun add(consumer: SerializableConsumer<in T>): RoutinePipeline<T> {
        return object : AbstractRoutinePipeline<T>(this) {
            @Throws(Exception::class)
            override fun run() {
                root!!.run()
            }

            @Throws(Exception::class)
            override fun _execute(target: T) {
                consumer.accept(target!!)
            }
        }
    }

    override fun append(routine: RoutinePipeline<T>?): RoutinePipeline<T> {
        next = routine
        return this
    }

    override fun attach(routine: RoutinePipeline<T>?): RoutinePipeline<T> {
        previous = routine
        return this
    }

    override fun concat(routines: RoutinePipeline<T>): RoutinePipeline<T> {
        return this
    }

    override fun insert(routine: RoutinePipeline<T>) {
        if (routine == null) return
        if (next != null) routine.append(next!!)
        routine.attach(this)
        if (hasNext()) {
            next!!.attach(routine)
        }
        next = routine
    }

    override fun interpose(routine: RoutinePipeline<T>) {
        if (routine == null) return
        if (previous != null) routine.attach(previous!!)
        routine.append(this)
        if (previous != null) {
            previous!!.append(routine)
        }
        previous = routine
    }

    override fun before(target: RoutinePipeline<T>): RoutinePipeline<T> {
        if (target == null) return this
        val prev: RoutinePipeline<T> = previous!!
        prev.append(next)
        target.interpose(this)
        return prev
    }

    override fun toFirst(): RoutinePipeline<T> {
        val prev: RoutinePipeline<T> = previous!!
        remove(this, false)
        // root.insert(this);
        root!!.migrate(this, Passage.Align.Front)
        return prev
    }

    override fun after(target: RoutinePipeline<T>): RoutinePipeline<T> {
        if (target == null) return this
        val prev: RoutinePipeline<T> = previous!!
        prev.append(next)
        target.insert(this)
        return prev
    }

    override fun toLast(): RoutinePipeline<T> {
        val prev: RoutinePipeline<T> = previous!!
        remove(this, false)
        // getLastRoutine().insert(this);
        root!!.migrate(this, Passage.Align.Back)
        return prev
    }

    override fun remove(target: RoutinePipeline<T>): RoutinePipeline<T> {
        return remove(target, true)
    }

    override fun remove(target: RoutinePipeline<T>, emptyRoot: Boolean): RoutinePipeline<T> {
        if (target == null) return this
        if (this == target) {
            val rtn: RoutinePipeline<T> = previous!!
            erase(emptyRoot)
            return rtn
        }
        target.remove(target, emptyRoot)
        return this
    }

    override fun getLastRoutine(): RoutinePipeline<T>? {
        return root!!.getLastRoutine()
    }

    private fun erase(emptyRoot: Boolean) {
        previous!!.append(next)
        if (hasNext()) {
            next!!.attach(previous)
        }
        previous = null
        next = null
        if (emptyRoot) {
            this.root = null
        }
    }

    open operator fun hasNext(): Boolean {
        return next != null
    }

    override fun whenever(condStmt: SerializablePredicate<T>): Condition<T> {
        return ConditionalRoutine<T>(this, condStmt)
    }

    override fun changeRoot(endpoint: PipelineEndpoint<T>?) {
        root = endpoint
    }

}