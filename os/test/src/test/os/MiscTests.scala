package test.os
import java.io.{BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader}

import utest._

import scala.util.Random
object MiscTests extends TestSuite{
  def tests = Tests{
    val loremIpsum = """
      Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
      eiusmod tempor incididunt ut labore et dolore magna aliqua.

      inim veniam, quis nostrud exercitation ullamco laboris
      nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in
      reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
      pariatur. Excepteur sint occaecat cupidatat non proident, sunt in
      culpa qui officia deserunt mollit anim id est laborum.
    """

    test("SubprocessOutput"){
      def readLines(readLine: () => String): Seq[String] = {
        val output = collection.mutable.ArrayBuffer[String]()
        var continue = true
        while (continue) {
          val line = readLine()
          if (line != null) output.append(line)
          else continue = false
        }
        output.toSeq
      }
      def check(input: String, expected: String*) = {
        val bufferSizes = Seq(
          // Test all fibonacci buffer sizes
          1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144,
          233, 377, 610, 987, 1597, 2584, 4181, 6765, 8192
        )

        for(bufferSize <- bufferSizes) {
          // println(bufferSize)
          val subOut = new os.SubProcess.OutputStream(
            new ByteArrayInputStream(input.getBytes), bufferSize
          )

          val br = new BufferedReader(
            new InputStreamReader(new ByteArrayInputStream(input.getBytes))
          )

          val output = readLines(() => subOut.readLine())
          val golden = readLines(() => br.readLine())

          if (expected.nonEmpty) assert(expected == output)
          assert(output == golden)
        }
      }

      test("hello"){
        test - check("hello world", "hello world")
        test - check("hello\nworld", "hello", "world")
        test - check("hello\rworld", "hello", "world")
        test - check("hello\r\nworld", "hello", "world")
        test - check("hello\r\nworld\r", "hello", "world")
        test - check("hello\r\nworld\r\n", "hello", "world")
        test - check("hello\r\nworld\n", "hello", "world")
        test - check("hello\r\nworld\n\n", "hello", "world", "")
        test - check("hello\r\nworld\n\r", "hello", "world", "")
        test - check("hello\r\nworld\r\r", "hello", "world", "")
      }
      // Fuzz testing various combinations of \n and \r
      test("newlines"){
        val random = new Random(0)
        test - check(Seq.fill(1)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(4)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(16)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(64)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(256)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(1024)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(4096)(if (random.nextBoolean()) '\n' else '\r').mkString)
        test - check(Seq.fill(16384)(if (random.nextBoolean()) '\n' else '\r').mkString)
      }
      // Fuzz testing various lengths of lorem ipsum
      test("lorem"){
        test - check(loremIpsum)
        test - check(loremIpsum * 4)
        test - check(loremIpsum * 16)
        test - check(loremIpsum * 64)
        test - check(loremIpsum * 256)
        test - check(loremIpsum * 1024)
      }
    }
  }
}
