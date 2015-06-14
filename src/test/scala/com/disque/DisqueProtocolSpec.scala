package com.disque

import org.scalatest.{MustMatchers, WordSpec}

case class TestProtocol(response: String) extends DisqueProtocol {
  var remainingMessage = response
  val SEP = "\r\n"

  override def readLine: Array[Byte] = {
//    println(s"Reading from: [${escape(remainingMessage)}]")
    val index = remainingMessage.indexOf(SEP)
    val line = remainingMessage.take(index)
    remainingMessage = remainingMessage.drop(index)
    if (remainingMessage.length > 0)
      remainingMessage = remainingMessage.drop(2)
//    println(s"Read line: [${line}]. Remaining: [${remainingMessage}] (${remainingMessage.length})")
    line.getBytes
  }
  override def readCounted(c: Int): Array[Byte] = {
    val chars = remainingMessage.take(c)
    remainingMessage = remainingMessage.drop(c)
    chars.getBytes
  }
}

class DisqueProtocolSpec extends WordSpec with MustMatchers {
  def getResponse(string: String): Response = {
    val prot = TestProtocol(string)
    prot.as(prot.anyResponse)
  }

  "Disque Protocol parsing" should {
    "simple messages" should {
      "support integers" in {
        getResponse(":100\r\n") must equal(Integer(100L))
      }

      "simple strings" in {
        getResponse("+OK\r\n") must equal(SimpleString("OK"))
      }

      "error messages" in {
        getResponse("-Error message 123\r\n") must equal(Error("Error message 123"))
      }
    }

    "complex messages" should {
      "bulk strings" in {
        getResponse("$6\r\nfoobar\r\n") must equal(BulkString("foobar"))
      }
      "bulk zero-length strings" in {
        getResponse("$0\r\n\r\n") must equal(BulkString(""))
      }
      "nulls" in {
        getResponse("$-1\r\n") must equal(NullResponse())
      }
      "empty arrays" in {
        getResponse("*0\r\n") must equal (Multi(List()))
      }
      "non-empty arrays" in {
        val resp = getResponse("*2\r\n$3\r\nfoo\r\n:100\r\n")
        resp mustEqual(Multi(List(BulkString("foo"), Integer(100L))))
      }
      "nested arrays" in {
        val message =
          """
            |*2
            |*3
            |:1
            |:2
            |:3
            |*2
            |+Foo
            |-Bar
          """.stripMargin.split("\n").tail.mkString("\r\n")
        getResponse(message) must equal(
          Multi(List(
            Multi(List(Integer(1L), Integer(2L), Integer(3L))),
            Multi(List(SimpleString("Foo"), Error("Bar")))
          ))
        )
      }
    }
  }
}
