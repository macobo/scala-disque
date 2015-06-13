package com.disque

import com.redis.Protocol
import com.redis.serialization.Parse.{Implicits => Parsers}

sealed trait Response
sealed trait StringResponse { def s: String }
case class SimpleString(s: String) extends Response with StringResponse
case class BulkString(s: String) extends Response with StringResponse
case class Integer(n: Long) extends Response
case class Error(reason: String) extends Response
case class Multi(results: List[Response]) extends Response

// Re-implement much of the protocols used in Redis lib for a saner interface and to allow nesting of
// multiResponses and to use a saner interface.
trait DisqueProtocol extends Protocol {
  val SINGLE = '+'
  val ERROR  = '-'
  val BULK   = '$'
  val MULTI  = '*'
  val INT    = ':'

  val longResponse: Reply[Integer] = {
    case (INT, s) => Integer(Parsers.parseLong(s))
  }

  val errorResponse: Reply[Error] = {
    case (ERROR, reason) => Error(Parsers.parseString(reason))
  }

  val simpleStringResponse: Reply[SimpleString] = {
    case (SINGLE, s) => SimpleString(Parsers.parseString(s))
  }

  val bulkResponse: Reply[BulkString] = {
    case (BULK, lengthStr) => {
      val n = Parsers.parseInt(lengthStr)
      val str = readCounted(n)
      val ignore = readLine // trailing newline
      BulkString(Parsers.parseString(str))
    }
  }

  val anyResponse: Reply[Response] =
    longResponse orElse errorResponse orElse simpleStringResponse orElse bulkResponse orElse multiResponse

  val multiResponse: Reply[Multi] = {
    case (MULTI, countStr) => {
      val n = Parsers.parseInt(countStr)
      val results = List.fill(n) { receive(anyResponse) }
      Multi(results)
    }
  }

  def as[T](responseType: Reply[T]): T =
    receive(responseType)
}
