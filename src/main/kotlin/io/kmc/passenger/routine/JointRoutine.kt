package io.kmc.passenger.routine


open class JointRoutine<T> : AbstractRoutinePipeline<T>, Joint<T> {

    // Entry<T> source;
    var fork: Passage<T>? = null

    constructor() {}

    constructor(previous: AbstractRoutinePipeline<T>,
                passage: Passage<T>) : super(previous) {
        fork = passage
    }

    constructor(entry: Passage.Entry<T>?) {
        fork = PipelineEndpoint.create<T>(entry)
    }

    @Throws(Exception::class)
    override fun _execute(target: T) {
        fork!!.execute(target)
    }

    override fun reset() {
        super.reset()
        fork?.reset()
    }

    @Throws(Exception::class)
    override fun run() {
        root?.run()
    }

    override fun steer(): Passage<T> {
        return fork!!
    }

}