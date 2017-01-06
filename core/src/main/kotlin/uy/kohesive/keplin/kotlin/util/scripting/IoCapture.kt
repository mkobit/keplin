package uy.kohesive.keplin.kotlin.util.scripting

import org.jetbrains.kotlin.cli.common.repl.InvokeWrapper
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.*

object InOutTrapper {
    init {
        System.setIn(ThreadAwareInputStreamForker(System.`in`))
        System.setOut(ThreadAwarePrintStreamForker(System.out))
        System.setErr(ThreadAwarePrintStreamForker(System.err))
    }

    fun trapSystemInOutForThread(input: InputStream, output: PrintStream, errOutput: PrintStream) {
        (System.`in` as? ThreadAwareInputStreamForker)?.pushThread(input)
        (System.out as? ThreadAwarePrintStreamForker)?.pushThread(output)
        (System.err as? ThreadAwarePrintStreamForker)?.pushThread(errOutput)
    }

    fun removeSystemInOutForThread() {
        (System.`in` as? ThreadAwareInputStreamForker)?.popThread()
        (System.out as? ThreadAwarePrintStreamForker)?.popThread()
        (System.err as? ThreadAwarePrintStreamForker)?.popThread()
    }

    fun trapSystemOutForThread(output: PrintStream) {
        (System.out as? ThreadAwarePrintStreamForker)?.pushThread(output)
    }

    fun trapSystemErrForThread(errOutput: PrintStream) {
        (System.err as? ThreadAwarePrintStreamForker)?.pushThread(errOutput)
    }

    fun trapSystemInForThread(input: InputStream) {
        (System.`in` as? ThreadAwareInputStreamForker)?.pushThread(input)
    }

    fun removeSystemOutForThread() {
        (System.out as? ThreadAwarePrintStreamForker)?.popThread()
    }

    fun removeSystemErrForThread() {
        (System.err as? ThreadAwarePrintStreamForker)?.popThread()
    }

    fun removeSystemInForThread() {
        (System.`in` as? ThreadAwareInputStreamForker)?.popThread()
    }
}




// TODO: if thread is in a thread group then we can trap all the child threads...  good
//       or bad idea?


open class RerouteScriptIoInvoker(val input: InputStream, val output: PrintStream, val errorOutput: PrintStream) : InvokeWrapper {
    override fun <T> invoke(body: () -> T): T {
        InOutTrapper.trapSystemInOutForThread(input, output, errorOutput)
        try {
            return body()
        } finally {
            InOutTrapper.removeSystemInOutForThread()
        }
    }
}

interface ThreadIoTrap<T : Any> {
    val fallback: T
    val traps: ThreadLocal<T>

    fun mine(): T = traps.get() ?: fallback

    fun pushThread(redirect: T) {
        traps.set(redirect)
    }

    fun popThread() {
        traps.remove()
    }
}

class IoTrapManager<T : Any>(override val fallback: T, override val traps: ThreadLocal<T> = ThreadLocal<T>()) : ThreadIoTrap<T>

class ThreadAwareInputStreamForker(fallbackInputStream: InputStream)
    : InputStream(), ThreadIoTrap<InputStream> by IoTrapManager(fallbackInputStream) {
    override fun read(): Int {
        return mine().read()
    }
}

class ThreadAwareOutputStreamForker(fallbackOutputStream: OutputStream)
    : OutputStream(), ThreadIoTrap<OutputStream> by IoTrapManager(fallbackOutputStream) {
    override fun write(b: Int) {
        mine().write(b)
    }
}

class ThreadAwarePrintStreamForker(fallbackPrintStream: PrintStream)
    : PrintStream(fallbackPrintStream), ThreadIoTrap<PrintStream> by IoTrapManager(fallbackPrintStream) {
    override fun flush() {
        mine().flush()
    }

    override fun checkError(): Boolean {
        return mine().checkError()
    }

    override fun write(b: Int) {
        mine().write(b)
    }

    override fun print(b: Boolean) {
        mine().print(b)
    }

    override fun print(c: Char) {
        mine().print(c)
    }

    override fun print(i: Int) {
        mine().print(i)
    }

    override fun print(l: Long) {
        mine().print(l)
    }

    override fun print(f: Float) {
        mine().print(f)
    }

    override fun print(d: Double) {
        mine().print(d)
    }

    override fun print(s: CharArray) {
        mine().print(s)
    }

    override fun print(s: String?) {
        mine().print(s)
    }

    override fun print(obj: Any?) {
        mine().print(obj)
    }

    override fun println() {
        mine().println()
    }

    override fun println(x: Boolean) {
        mine().println(x)
    }

    override fun println(x: Char) {
        mine().println(x)
    }

    override fun println(x: Int) {
        mine().println(x)
    }

    override fun println(x: Long) {
        mine().println(x)
    }

    override fun println(x: Float) {
        mine().println(x)
    }

    override fun println(x: Double) {
        mine().println(x)
    }

    override fun println(x: CharArray) {
        mine().println(x)
    }

    override fun println(x: String?) {
        mine().println(x)
    }

    override fun println(x: Any?) {
        mine().println(x)
    }

    override fun append(csq: CharSequence?): PrintStream {
        return mine().append(csq)
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): PrintStream {
        return mine().append(csq, start, end)
    }

    override fun append(c: Char): PrintStream {
        return mine().append(c)
    }

    override fun format(format: String, vararg args: Any?): PrintStream {
        return mine().format(format, *args)
    }

    override fun format(l: Locale?, format: String, vararg args: Any?): PrintStream {
        return mine().format(l, format, *args)
    }

    override fun printf(format: String, vararg args: Any?): PrintStream {
        return mine().printf(format, *args)
    }

    override fun printf(l: Locale?, format: String, vararg args: Any?): PrintStream {
        return mine().printf(l, format, *args)
    }

    override fun close() {
        throw UnsupportedOperationException("close() not allowed")
    }

    override fun write(buf: ByteArray, off: Int, len: Int) {
        mine().write(buf, off, len)
    }
}
