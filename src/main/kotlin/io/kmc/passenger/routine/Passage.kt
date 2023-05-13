package io.kmc.passenger.routine

import java.io.Serializable

interface Passage<T> : Serializable {
    
    fun applyTo(target: T?): Passage<T>
    
    fun execute(target: T)
    
    fun run()
    
    fun reset()
    
    fun integrate(passage: Passage<T>): Passage<T>
    
    fun join(passage: Passage<T>): RoutinePipeline<T>

    fun add(routine: RoutinePipeline<T>): RoutinePipeline<T>

    fun add(clazzName: String, methodName: String, vararg args: Serializable): ReflectRoutine<T>

    // ReflectRoutine<T> add(String clazzName, String methodName, Integer targetIndex, Serializable... args);

    // ReflectRoutine<T> add(String clazzName, String methodName, Integer targetIndex, Serializable... args);

    fun <R> add(func: SerializableFunction<in T, out R?>): RoutinePipeline<T>

    fun add(func: SerializableConsumer<in T>): RoutinePipeline<T>

    fun whenever(conditional: SerializablePredicate<T>): Condition<T>

    companion object Factory {
        @JvmStatic
        fun <T> create(): Passage<T> {
            return Passage.Entry()
        }
    }

    class Entry<T> internal constructor() : Passage<T> {
        var target: T? = null
        var start: RoutinePipeline<T>
        var front: JointRoutine<T>? = null
        var middle: JointRoutine<T>
        var back: JointRoutine<T>? = null
        var end: RoutinePipeline<T>

        init {
            middle = JointRoutine(this)
            start = middle
            end = middle
        }

        override fun join(passage: Passage<T>): RoutinePipeline<T> {
            return middle.steer().join(passage)
        }

        override fun integrate(passage: Passage<T>): Passage<T> {
            if (passage is Entry<*>) {
                val external = passage as Entry<T>
                var routines = external.getAlignedRoutines(Align.Front)
                if (routines != null) {
                    start.interpose(routines)
                    start = routines
                }
                routines = external.getAlignedRoutines(Align.Middle)
                middle.insert(routines!!)
                routines = external.getAlignedRoutines(Align.Back)
                if (routines != null) {
                    end.insert(routines)
                    end = routines
                }
            }
            return this
        }

        override fun add(clazzName: String, methodName: String, vararg args: Serializable): ReflectRoutine<T> {
            return middle.steer().add(clazzName, methodName, *args)
        }

        // @Override
        // public ReflectRoutine<T> add(String clazzName, String methodName, Integer targetIndex, Serializable... args) {
        //     return middle.steer().add(clazzName, methodName, targetIndex, args);
        // }

        override fun add(routine: RoutinePipeline<T>): RoutinePipeline<T> {
            return middle.steer().add(routine)
        }

        fun add(
            routine: RoutinePipeline<T>,
            align: Align
        ): RoutinePipeline<T> {
            return getAlignedRoutines(align, true)!!.steer().add(routine)
        }

        override fun <R> add(func: SerializableFunction<in T, out R?>): RoutinePipeline<T> {
            return middle.steer().add(func)
        }

        override fun add(func: SerializableConsumer<in T>): RoutinePipeline<T> {
            return middle.steer().add(func)
        }

        override fun whenever(conditional: SerializablePredicate<T>): Condition<T> {
            return middle.steer().whenever(conditional)
        }

        protected fun getAlignedRoutines(align: Align?, force: Boolean): JointRoutine<T>? {
            return when (align) {
                Align.Front -> {
                    if (front == null && force) {
                        val entry = this
                        front = object : JointRoutine<T>() {
                            init {
                                fork = object : PipelineEndpoint<T>(entry) {
                                    var end: RoutinePipeline<T>? = null

                                    init {
                                        val head = this
                                        end = object : PipelineEndpoint<T>(head) {
                                            public override fun _execute(target: T) {
                                                // End of pipeline
                                            }

                                            override fun getLastRoutine(): RoutinePipeline<T>? {
                                                return previous
                                            }
                                        }
                                    }

                                    override fun add(routine: RoutinePipeline<T>): RoutinePipeline<T> {
                                        insert(routine)
                                        return routine
                                    }

                                    override fun getLastRoutine(): RoutinePipeline<T>? {
                                        return end?.getLastRoutine()
                                    }
                                }
                            }
                        }
                        front?.let{
                            middle.interpose(it)
                            if (start === middle) {
                                start = it
                            }
                        }
                    }
                    front
                }

                Align.Back -> {
                    if (back == null && force) {
                        back = JointRoutine(this)
                        back?.let{
                            middle.insert(it)
                            if (end === middle) {
                                end = it
                            }
                        }

                    }
                    back
                }

                Align.Middle -> middle
                else -> middle
            }
        }

        override fun applyTo(target: T?): Passage<T> {
            this.target = target
            return this
        }

        /**
         * May casue issue when two routines are joined but triggering run() at second part of passage entry.
         * The routine pipeline will be executed starting from the second source even though they are already joined.
         */
        @Throws(Exception::class)
        override fun run() {
            if (target == null) return
            execute(target!!)
            reset()
        }

        @Throws(Exception::class)
        override fun execute(target: T) {
            _execute(target)
        }

        @Throws(Exception::class)
        protected fun _execute(target: T) {
            start.execute(target)
        }

        override fun reset() {
            cleanTarget()
            start.reset()
        }

        protected fun cleanTarget() {
            applyTo(null)
        }

        val lastRoutine: RoutinePipeline<T>?
            get() = back?.getLastRoutine()

        fun getAlignedRoutines(align: Align?): RoutinePipeline<T>? {
            return getAlignedRoutines(align, false)
        } 
        
    }

    enum class Align {
        Front, Middle, Back
    }

}