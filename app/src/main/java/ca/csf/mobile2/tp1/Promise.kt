package ca.csf.mobile2.tp1

class Promise<R, E> private constructor(val result: R?, val error: E?) {

    val isSuccessful: Boolean
        get() = error == null

    companion object {

        fun <R, E> ok(result: R): Promise<R, E> {
            return Promise(result, null)
        }

        fun <R, E> err(error: E): Promise<R, E> {
            return Promise(null, error)
        }
    }

}
