package io.kmc.passenger.routine


interface RoutinePipeline<T> : Passage<T> {
    /**
     * Adding a relationship that the next routine of current routine will point to the new routine.
     * For instance, applying A.append(new) will get:
     * A.next -> new
     * @param routine
     * @return The current routine in the pipeline
     */
    fun append(routine: RoutinePipeline<T>?): RoutinePipeline<T>

    /**
     * Adding a relationship that the previous routine of current routine will point to the new routine.
     * For instance, applying A.attach(new) will get:
     * new <- A.previous
     * @param routine
     * @return The current routine in the pipeline
     */
    fun attach(routine: RoutinePipeline<T>?): RoutinePipeline<T>

    fun concat(routines: RoutinePipeline<T>): RoutinePipeline<T>

    fun insert(routine: RoutinePipeline<T>)

    fun interpose(routine: RoutinePipeline<T>)

    /**
     * Appending current routine before target routine.
     * For instance,
     * source -> A -> B -> C -> D -> Current -> end
     * If applying Current.before(B),
     * source -> A -> Current -> B -> C -> D -> end
     *
     * @param target
     * @return The routine at the end of routine pipeline
     */
    fun before(target: RoutinePipeline<T>): RoutinePipeline<T>

    /**
     * Moving current routine to the first of the routine pipeline
     * For instance,
     * source -> A -> B -> C -> Current -> end
     * If applying Current.toFirst(),
     * source -> Current -> A -> B -> C -> end
     * @return The routine originally previosu to the current routine.
     * For the case of the example, 'C' will be returned
     */
    fun toFirst(): RoutinePipeline<T>

    /**
     * Concatenating current routine after target routine.
     * For instance,
     * source -> A -> B -> C -> D -> Current -> end
     * If applying Current.after(B),
     * source -> A -> B -> Current -> C -> D -> end
     *
     * @param target
     * @return The routine at the end of routine pipeline
     */
    fun after(target: RoutinePipeline<T>): RoutinePipeline<T>

    /**
     * Moving current routine to the last of the routine pipeline
     * For instance,
     * source -> A -> B -> C -> Current -> D -> end
     * If applying Current.toFirst(),
     * source -> A -> B -> C -> D -> Current -> end
     * @return The routine originally previosu to the current routine.
     * For the case of the example, 'C' will be returned
     */
    fun toLast(): RoutinePipeline<T>

    /**
     * Removing target routine.
     * @param target
     * @return
     */
    fun remove(target: RoutinePipeline<T>): RoutinePipeline<T>

    fun remove(target: RoutinePipeline<T>, emptyRoot: Boolean): RoutinePipeline<T>

    fun getLastRoutine(): RoutinePipeline<T>?

    fun changeRoot(endpoint: PipelineEndpoint<T>?)

}