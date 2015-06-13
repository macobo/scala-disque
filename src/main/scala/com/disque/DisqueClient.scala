package com.disque

import com.disque.commands.AddRemoveJobs
import com.redis.Redis
import com.redis.serialization.Format
import com.redis.serialization.Parse.{Implicits => Parsers}

trait Disque extends Redis with DisqueProtocol

trait DisqueCommand
extends Disque
with AddRemoveJobs
{
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

  def enqueue(queue: String, job: String)(implicit format: Format) = {}
}
