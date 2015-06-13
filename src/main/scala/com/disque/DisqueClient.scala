package com.disque

import com.redis.serialization.Format
import com.redis.serialization.Parse.{Implicits => Parsers}
import com.redis.{Protocol, Redis}




trait Disque extends Redis with DisqueProtocol


trait DisqueCommand extends Disque with DisqueProtocol {
  override def initialize: Boolean = connect

}

class DisqueClient(
  override val host: String = "localhost",
  override val port: Int = 7711,
  override val timeout: Int = 0
)
extends DisqueCommand
{
  def hello()(implicit format: Format): Multi = {
    send("HELLO")(as(multiResponse))
  }
}
